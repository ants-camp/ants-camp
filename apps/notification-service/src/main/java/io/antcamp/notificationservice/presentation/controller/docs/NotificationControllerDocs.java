package io.antcamp.notificationservice.presentation.controller.docs;

import io.antcamp.notificationservice.presentation.dto.request.PrometheusWebhookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Notification", description = "Prometheus AlertManager 웹훅 수신 / Slack 인터랙션 처리 (내부 전용)")
public interface NotificationControllerDocs {

    @Operation(summary = "Prometheus AlertManager 웹훅 수신",
            description = "AlertManager가 발송하는 웹훅을 수신하여 Slack 알림을 전송합니다. Bearer Secret 인증 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수신 성공 (알림 처리 진행)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = ""))),
            @ApiResponse(responseCode = "401", description = "Bearer Secret 불일치",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = "")))
    })
    @PostMapping("/prometheus")
    ResponseEntity<Void> receivePrometheusAlert(
            @Parameter(description = "Bearer {webhookSecret}", in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PrometheusWebhookRequest request);

    @Operation(summary = "Slack 버튼 액션 처리",
            description = """
                    Slack Interactive Message의 버튼 클릭 이벤트를 처리합니다.
                    Slack은 3초 내 200 응답을 요구하므로, 처리 오류가 발생해도 항상 200을 반환합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "항상 200 반환 (Slack 정책)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = "")))
    })
    @PostMapping(value = "/interactions", consumes = "application/x-www-form-urlencoded")
    ResponseEntity<Void> receiveSlackAction(HttpServletRequest request);
}
