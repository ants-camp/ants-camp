package io.antcamp.notificationservice.application.service;

import common.exception.ErrorCode;
import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.application.dto.command.SlackActionCommand;
import io.antcamp.notificationservice.application.port.ActionResult;
import io.antcamp.notificationservice.application.port.AlertPort;
import io.antcamp.notificationservice.application.port.CachePort;
import io.antcamp.notificationservice.application.port.DeduplicationPort;
import io.antcamp.notificationservice.application.port.LlmPort;
import io.antcamp.notificationservice.application.port.LogPort;
import io.antcamp.notificationservice.application.port.MonitoringPort;
import io.antcamp.notificationservice.application.port.RestartPort;
import io.antcamp.notificationservice.application.port.RollbackPort;
import io.antcamp.notificationservice.domain.exception.DockerOperationException;
import io.antcamp.notificationservice.domain.exception.NotificationException;
import io.antcamp.notificationservice.domain.model.*;
import io.antcamp.notificationservice.domain.repository.NotificationRepository;
import io.antcamp.notificationservice.infrastructure.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;
    private final AlertPort alertPort;
    private final CachePort cachePort;
    private final RestartPort restartPort;
    private final RollbackPort rollbackPort;
    private final LlmPort llmPort;
    private final MonitoringPort monitoringPort;
    private final LogPort logPort;
    private final AlertContentBuilder alertContentBuilder;
    private final DeduplicationPort deduplicationPort;
    private final NotificationCommandHandler notificationCommandHandler;
    private final NotificationProperties properties;

    @Value("${slack.channel-id}")
    private String channelId;

    public void handlePrometheusAlert(PrometheusAlertCommand command) {
        for (PrometheusAlertCommand.AlertItem alert : command.alerts()) {
            if (!alert.firing()) {
                log.info("RESOLVED(해결된) 알림 무시: {}", alert.alertName());
                continue;
            }
            try {
                processAlert(alert);
            } catch (Exception e) {
                log.error("알림 처리 실패, 다음 알림으로 진행: alertName={}, error={}", alert.alertName(), e.getMessage());
            }
        }
    }

    private void processAlert(PrometheusAlertCommand.AlertItem alert) {
        String dedupKey = alert.fingerprint();
        if (!deduplicationPort.tryReserve("alert:dedup:" + dedupKey, Duration.ofHours(1))) {
            log.info("중복 알림 무시: fingerprint={}", dedupKey);
            return;
        }

        MonitoringMetrics metrics = monitoringPort.collectMetrics(alert.job());
        String recentLogs = logPort.collectRecentLogs(alert.job());

        String aiAnalysis = null;
        if (metrics.isValid()) {
            aiAnalysis = llmPort.analyze(alert, metrics, recentLogs);
        } else {
            log.warn("메트릭 데이터 이상으로 LLM 분석 생략: job={}", alert.job());
        }

        Notification savedNotification = notificationRepository.save(
                Notification.create(
                        channelId,
                        alert.job(),
                        AlertSource.PROMETHEUS,
                        dedupKey,
                        AlertSeverity.from(alert.severity()),
                        alert.summary() != null ? alert.summary() : alert.alertName(),
                        alertContentBuilder.buildContent(alert),
                        alertContentBuilder.serializePayload(alert),
                        aiAnalysis != null ? aiAnalysis : ""
                )
        );

        try {
            String slackTs = alertPort.send(savedNotification);
            savedNotification.markAsSent(slackTs);
        } catch (Exception e) {
            log.error("Slack 전송 실패: {}", e.getMessage());
            savedNotification.markAsFailed();
        }

        try {
            notificationRepository.save(savedNotification);
        } catch (Exception e) {
            log.error("알림 상태 저장 실패 — DB/Slack 불일치 가능: notificationId={}, status={}, slackTs={}",
                    savedNotification.getNotificationId(), savedNotification.getStatus(),
                    savedNotification.getSlackMessageTs(), e);
        }
    }

    public boolean recordSlackAction(SlackActionCommand command) {
        String userEmail = alertPort.getUserEmail(command.slackUserId());
        try {
            //슬랙 액션 저장
            notificationCommandHandler.recordAction(command.notificationId(), command.action(), userEmail);
            return true;
        } catch (NotificationException e) {
            if (e.getErrorCode() == ErrorCode.NOTIFICATION_ALREADY_HANDLED) {
                log.info("이미 처리된 알림 버튼 클릭 무시: notificationId={}, userId={}", command.notificationId(), command.slackUserId());
                return false;
            }
            throw e;
        }
    }

    /**
     * 사용자 interaction 비동기처리
     * - 도커 작업들이 시간소요가 많아 슬랙 응답시간인 3초를 초과하여 슬랙에 오류 표기됨
     * - 실제로 처리 진행중인 동안 사용자 입장에서 진행중인지, 오류인지 알 방법이 없음
     */
    @Async("slackActionExecutor")
    public void executeAndNotifyAsync(SlackActionCommand command) {
        Notification notification = notificationRepository.findById(command.notificationId())
                .orElseThrow(() -> {
                    log.warn("비동기 처리 중 알림을 찾을 수 없음: notificationId={}", command.notificationId());
                    return NotificationException.notFound();
                });

        trySlack("처리 중 메시지 갱신", () ->
                alertPort.markAsProcessing(notification, command.slackUserId(), command.action()));

        ActionResult result = executeAction(command.action(), notification.getJob());

        if (result instanceof ActionResult.Failure) {
            notificationCommandHandler.markActionFailed(command.notificationId());
        }

        boolean succeeded = result instanceof ActionResult.Success;
        trySlack("Slack 메시지 업데이트", () ->
                alertPort.markAsHandled(notification, command.slackUserId(), command.action(), succeeded));
        trySlack("스레드 답글 전송", () ->
                alertPort.notifyActionResult(notification.getChannelId(), notification.getSlackMessageTs(),
                        command.action(), command.slackUserId(), result));
    }

    private void trySlack(String taskName, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Slack 처리 실패 [{}]: {}", taskName, e.getMessage());
        }
    }

    private ActionResult executeAction(ResolutionAction action, String job) {
        return switch (action) {
            case CACHE_CLEAR -> {
                try {
                    cachePort.clear(job);
                    yield new ActionResult.Success();
                } catch (Exception e) {
                    log.error("캐시 비우기 실패: {}", e.getMessage());
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                }
            }
            case RESTART -> {
                if (properties.infrastructureJobs().contains(job)) {
                    log.warn("인프라 서비스 재시작 시도 차단: job={}", job);
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                }
                try {
                    restartPort.restart(job);
                    yield new ActionResult.Success();
                } catch (Exception e) {
                    log.error("서비스 재시작 실패: {}", e.getMessage());
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                }
            }
            case ROLLBACK -> {
                if (properties.infrastructureJobs().contains(job)) {
                    log.warn("인프라 서비스 롤백 시도 차단: job={}", job);
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                }
                try {
                    rollbackPort.rollback(job);
                    yield new ActionResult.Success();
                } catch (DockerOperationException e) {
                    if (e.getErrorCode() == ErrorCode.ROLLBACK_IMAGE_NOT_CONFIGURED) {
                        log.warn("롤백 이미지 미설정: job={}", job);
                        yield new ActionResult.Failure(ActionResult.FailureReason.NOT_CONFIGURED);
                    }
                    log.error("코드 롤백 실패: {}", e.getMessage());
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                } catch (Exception e) {
                    log.error("코드 롤백 실패: {}", e.getMessage());
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                }
            }
            case FALSE_ALARM -> new ActionResult.Success();
        };
    }
}