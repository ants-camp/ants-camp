package io.antcamp.notificationservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertContentBuilder {

    @Value("${grafana.url}")
    private String grafanaUrl;

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
        return UriComponentsBuilder.fromUriString(grafanaUrl + "/explore")
                .queryParam("orgId", "1")
                .queryParam("left", buildExploreJson(job))
                .build()
                .toUriString();
    }

    private static String grafanaCpuExpr(String job) {
        return String.format("process_cpu_usage{job=\"%s\"}", job);
    }

    private static String grafanaHeapExpr(String job) {
        return String.format(
                "jvm_memory_used_bytes{job=\"%s\",area=\"heap\"} / jvm_memory_max_bytes{job=\"%s\",area=\"heap\"} > 0",
                job, job);
    }

    private static String grafanaHttpErrorExpr(String job) {
        return String.format(
                "sum(rate(http_server_requests_seconds_count{job=\"%s\",status=~\"[45]..\"}[5m]))",
                job);
    }

    private String buildExploreJson(String job) {
        Map<String, Object> datasource = Map.of("type", "prometheus", "uid", "prometheus");

        List<Map<String, Object>> queries = List.of(
                Map.of("refId", "A", "expr", grafanaCpuExpr(job),
                        "instant", false, "range", true, "datasource", datasource),
                Map.of("refId", "B", "expr", grafanaHeapExpr(job),
                        "instant", false, "range", true, "datasource", datasource),
                Map.of("refId", "C", "expr", grafanaHttpErrorExpr(job),
                        "instant", false, "range", true, "datasource", datasource)
        );
        Map<String, Object> payload = Map.of(
                "datasource", "prometheus",
                "queries", queries,
                "range", Map.of("from", "now-1h", "to", "now")
        );
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Explore JSON 직렬화 실패: {}", e.getMessage());
            return "{}";
        }
    }
}