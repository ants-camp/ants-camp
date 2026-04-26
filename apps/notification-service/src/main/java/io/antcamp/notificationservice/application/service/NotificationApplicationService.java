package io.antcamp.notificationservice.application.service;

import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.application.dto.command.SlackActionCommand;
import io.antcamp.notificationservice.application.port.ActionResult;
import io.antcamp.notificationservice.application.port.AlertPort;
import io.antcamp.notificationservice.application.port.CachePort;
import io.antcamp.notificationservice.application.port.LlmPort;
import io.antcamp.notificationservice.application.port.LogPort;
import io.antcamp.notificationservice.application.port.MonitoringPort;
import io.antcamp.notificationservice.application.port.RestartPort;
import io.antcamp.notificationservice.application.port.RollbackPort;
import io.antcamp.notificationservice.domain.model.*;
import io.antcamp.notificationservice.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${slack.channel-id}")
    private String channelId;

    public void handlePrometheusAlert(PrometheusAlertCommand command) {
        for (PrometheusAlertCommand.AlertItem alert : command.alerts()) {
            if (!alert.firing()) {
                log.info("RESOLVED(해결된) 알림 무시: {}", alert.alertName());
                continue;
            }
            processAlert(alert);
        }
    }

    private void processAlert(PrometheusAlertCommand.AlertItem alert) {
        String dedupKey = alert.fingerprint();

        if (notificationRepository.existsSentByDeduplicationKey(dedupKey)) {
            log.info("이미 전송된 중복 알림 존재 : fingerprint={}", dedupKey);
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

        notificationRepository.save(savedNotification);
    }

    @Transactional
    public void handleSlackAction(SlackActionCommand command) {
        Notification notification = notificationRepository.findById(command.notificationId())
                .orElseThrow(() -> new IllegalArgumentException("해당 알림을 찾을 수 없습니다: " + command.notificationId()));

        String userEmail = alertPort.getUserEmail(command.slackUserId());

        try {
            notification.recordAction(command.action(), userEmail);
        } catch (IllegalStateException e) {
            log.info("이미 처리된 알림 버튼 클릭 무시: notificationId={}, userId={}", command.notificationId(), command.slackUserId());
            return;
        }

        notificationRepository.save(notification);
        log.info("액션 기록 완료: notificationId={}, action={}, userId={}", command.notificationId(), command.action(), command.slackUserId());

        ActionResult result = executeAction(command.action(), notification.getJob());

        if (result instanceof ActionResult.Failure) {
            notification.markActionFailed();
            notificationRepository.save(notification);
        }

        boolean succeeded = result instanceof ActionResult.Success;

        try {
            alertPort.markAsHandled(notification, command.slackUserId(), command.action(), succeeded);
        } catch (Exception e) {
            log.error("Slack 메시지 업데이트 실패: {}", e.getMessage());
        }

        try {
            alertPort.notifyActionResult(notification.getChannelId(), notification.getSlackMessageTs(),
                    command.action(), command.slackUserId(), result);
        } catch (Exception e) {
            log.error("스레드 답글 전송 실패: {}", e.getMessage());
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
                try {
                    restartPort.restart(job);
                    yield new ActionResult.Success();
                } catch (Exception e) {
                    log.error("서비스 재시작 실패: {}", e.getMessage());
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                }
            }
            case ROLLBACK -> {
                try {
                    rollbackPort.rollback(job);
                    yield new ActionResult.Success();
                } catch (IllegalStateException e) {
                    log.warn("롤백 이미지 미설정: {}", e.getMessage());
                    yield new ActionResult.Failure(ActionResult.FailureReason.NOT_CONFIGURED);
                } catch (Exception e) {
                    log.error("코드 롤백 실패: {}", e.getMessage());
                    yield new ActionResult.Failure(ActionResult.FailureReason.EXECUTION_ERROR);
                }
            }
            case FALSE_ALARM -> new ActionResult.Success();
        };
    }
}