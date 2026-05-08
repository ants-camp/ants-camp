package io.antcamp.notificationservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertContentBuilder {

    private final ObjectMapper objectMapper;

    public String buildContent(PrometheusAlertCommand.AlertItem alert) {
        return String.format(
                "서비스: %s | URI: %s | 예외: %s | HTTP: %s",
                alert.job(),
                alert.uri() != null ? alert.uri() : "-",
                alert.exception() != null ? alert.exception() : "-",
                alert.httpStatus() != null ? alert.httpStatus() : "-"
        );
    }

    public String serializePayload(PrometheusAlertCommand.AlertItem alert) {
        try {
            return objectMapper.writeValueAsString(alert);
        } catch (JsonProcessingException e) {
            log.warn("Alert 직렬화 실패: {}", e.getMessage());
            return "{}";
        }
    }
}