package io.antcamp.notificationservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertContentBuilder {

    @Value("${grafana.url}")
    private String monitoringBaseUrl;

    private final ObjectMapper objectMapper;

    public String buildContent(PrometheusAlertCommand.AlertItem alert) {
        String monitoringUrl = buildMonitoringUrl(alert.job());
        return String.format(
                "서비스: %s | URI: %s | 예외: %s | HTTP: %s | 모니터링: %s",
                alert.job(),
                alert.uri() != null ? alert.uri() : "-",
                alert.exception() != null ? alert.exception() : "-",
                alert.httpStatus() != null ? alert.httpStatus() : "-",
                monitoringUrl
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

    private String buildMonitoringUrl(String job) {
        return UriComponentsBuilder.fromUriString(monitoringBaseUrl + "/explore")
                .queryParam("orgId", "1")
                .queryParam("left", buildExploreJson(job))
                .build()
                .toUriString();
    }

    private String buildExploreJson(String job) {
        String queries = String.format(
                "[" +
                "{\"expr\":\"process_cpu_usage{job=\\\"%s\\\"}\",\"refId\":\"A\"}," +
                "{\"expr\":\"jvm_memory_used_bytes{job=\\\"%s\\\",area=\\\"heap\\\"} / jvm_memory_max_bytes{job=\\\"%s\\\",area=\\\"heap\\\"}\",\"refId\":\"B\"}," +
                "{\"expr\":\"sum(rate(http_server_requests_seconds_count{job=\\\"%s\\\",status=~\\\"[45]..\\\"}[5m]))\",\"refId\":\"C\"}" +
                "]",
                job, job, job, job
        );
        return String.format(
                "{\"datasource\":\"Prometheus\",\"queries\":%s,\"range\":{\"from\":\"now-1h\",\"to\":\"now\"}}",
                queries
        );
    }
}