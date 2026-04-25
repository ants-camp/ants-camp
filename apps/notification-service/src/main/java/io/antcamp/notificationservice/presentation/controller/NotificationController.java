package io.antcamp.notificationservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.application.dto.command.SlackActionCommand;
import io.antcamp.notificationservice.application.service.NotificationApplicationService;
import io.antcamp.notificationservice.domain.model.ResolutionAction;
import io.antcamp.notificationservice.presentation.dto.request.PrometheusWebhookRequest;
import io.antcamp.notificationservice.presentation.dto.request.SlackInteractivePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;
    private final ObjectMapper objectMapper;

    @Value("${webhook.secret:}")
    private String webhookSecret;

    /**
     * Alertmanager webhook 수신
     */
    @PostMapping("/prometheus")
    public ResponseEntity<Void> receivePrometheusAlert(
            @RequestParam(value = "secret", required = false) String secret,
            @RequestBody PrometheusWebhookRequest request) {
        if (!webhookSecret.isBlank() && !webhookSecret.equals(secret)) {
            log.warn("Prometheus webhook 인증 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationApplicationService.handlePrometheusAlert(PrometheusAlertCommand.from(request));
        return ResponseEntity.ok().build();
    }

    /**
     * Slack 버튼 클릭 처리
     */
    @PostMapping(value = "/interactions", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> receiveSlackAction(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String body = new String(request.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String payload = null;
            for (String pair : body.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2 && "payload".equals(java.net.URLDecoder.decode(parts[0], java.nio.charset.StandardCharsets.UTF_8))) {
                    payload = java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8);
                    break;
                }
            }
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

            ResolutionAction resolutionAction = ResolutionAction.valueOf(action.value());
            notificationApplicationService.handleSlackAction(
                    new SlackActionCommand(notificationId, slackUserId, resolutionAction)
            );
        } catch (Exception e) {
            // Slack은 3초 내 200 응답을 요구하므로 예외가 발생해도 200 반환
            log.error("Slack 액션 처리 실패: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 의도적 예외 발생 (테스트용)
     */
    @GetMapping("/test")
    public String test() {
        throw new RuntimeException("테스트용 의도적 예외 발생");
    }
}