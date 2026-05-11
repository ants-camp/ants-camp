package io.antcamp.notificationservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.CommonResponse;
import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.application.dto.command.SlackActionCommand;
import io.antcamp.notificationservice.application.dto.response.NotificationDetailResponse;
import io.antcamp.notificationservice.application.dto.response.NotificationSummaryResponse;
import io.antcamp.notificationservice.application.service.NotificationApplicationService;
import io.antcamp.notificationservice.application.service.NotificationQueryService;
import io.antcamp.notificationservice.domain.repository.PageResult;
import io.antcamp.notificationservice.presentation.dto.request.NotificationSearchRequest;
import io.antcamp.notificationservice.presentation.dto.request.PrometheusWebhookRequest;
import io.antcamp.notificationservice.presentation.dto.request.SlackInteractivePayload;
import io.antcamp.notificationservice.presentation.controller.docs.NotificationControllerDocs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerDocs {

    private final NotificationApplicationService notificationApplicationService;
    private final NotificationQueryService notificationQueryService;
    private final ObjectMapper objectMapper;

    @Value("${webhook.secret:}")
    private String webhookSecret;

    /**
     * Alertmanager webhook 수신
     */
    @PostMapping("/prometheus")
    public ResponseEntity<Void> receivePrometheusAlert(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PrometheusWebhookRequest request) {
        if (!webhookSecret.isBlank() && !("Bearer " + webhookSecret).equals(authHeader)) {
            log.warn("Prometheus webhook 인증 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PrometheusAlertCommand.AlertItem> items = request.alerts().stream()
                .map(alert -> new PrometheusAlertCommand.AlertItem(
                        alert.getLabel("alertname"),
                        alert.getLabel("severity"),
                        alert.getLabel("job"),
                        alert.getLabel("instance"),
                        alert.getLabel("uri"),
                        alert.getLabel("exception"),
                        alert.getLabel("status"),
                        alert.getAnnotation("summary"),
                        alert.getAnnotation("description"),
                        alert.fingerprint(),
                        alert.isFiring(),
                        alert.startsAt()
                ))
                .toList();
        notificationApplicationService.handlePrometheusAlert(
                new PrometheusAlertCommand(request.receiver(), request.status(), items));
        return ResponseEntity.ok().build();
    }

    /**
     * Slack 버튼 클릭 처리
     */
    @PostMapping(value = "/interactions", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> receiveSlackAction(HttpServletRequest request) {
        try {
            String payload = request.getParameter("payload");
            if (payload == null) {
                log.warn("Slack 액션 payload 없음");
                return ResponseEntity.ok().build();
            }
            SlackInteractivePayload slackPayload = objectMapper.readValue(payload, SlackInteractivePayload.class);

            UUID notificationId = slackPayload.notificationId();
            String slackUserId = slackPayload.slackUserId();
            SlackInteractivePayload.Action action = slackPayload.firstAction();

            if (notificationId == null || slackUserId == null || action == null) {
                log.warn("Slack 액션 payload 파싱 실패: {}", payload);
                return ResponseEntity.ok().build();
            }

            SlackActionCommand command = SlackActionCommand.of(notificationId, slackUserId, action.value());
            if (notificationApplicationService.recordSlackAction(command)) {
                notificationApplicationService.executeAndNotifyAsync(command);
            }
        } catch (Exception e) {
            // Slack은 3초 내 200 응답을 요구하므로 예외가 발생해도 200 반환
            log.error("Slack 액션 처리 실패: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 목록 조회
     */
    @GetMapping("/admin")
    public ResponseEntity<CommonResponse<PageResult<NotificationSummaryResponse>>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Role", required = false) String role,
            @ModelAttribute NotificationSearchRequest request,
            @RequestParam(defaultValue = "0") int page) {
        log.info("알림 목록 조회: userId={}, role={}", userId, role);
        return CommonResponse.ok("알림 목록 조회에 성공했습니다.",
                notificationQueryService.search(request.toQuery(page)));
    }

    /**
     * 알림 상세 조회
     */
    @GetMapping("/admin/{notificationId}")
    public ResponseEntity<CommonResponse<NotificationDetailResponse>> detail(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Role", required = false) String role,
            @PathVariable UUID notificationId) {
        log.info("알림 상세 조회: userId={}, role={}, notificationId={}", userId, role, notificationId);
        return CommonResponse.ok("알림 상세 조회에 성공했습니다.",
                notificationQueryService.findById(notificationId));
    }

}