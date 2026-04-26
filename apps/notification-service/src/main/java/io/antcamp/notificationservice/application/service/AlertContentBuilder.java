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

    private String buildExploreJson(String job) {
        Map<String, Object> datasource = Map.of("type", "prometheus", "uid", "prometheus");

        List<Map<String, Object>> queries = List.of(
                Map.of("refId", "A", "expr", PromQLQueries.cpu(job),
                        "instant", false, "range", true, "datasource", datasource),
                Map.of("refId", "B", "expr", PromQLQueries.heap(job),
                        "instant", false, "range", true, "datasource", datasource),
                Map.of("refId", "C", "expr", PromQLQueries.httpErrorRate(job),
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