package io.antcamp.notificationservice.infrastructure.client.monitoring;

import io.antcamp.notificationservice.application.port.MonitoringPort;
import io.antcamp.notificationservice.application.service.PromQLQueries;
import io.antcamp.notificationservice.domain.model.MonitoringMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusApiClient implements MonitoringPort {

    private static final String QUERY_PATH = "/api/v1/query";

    private final RestClient restClient;

    @Value("${prometheus.url}")
    private String prometheusUrl;

    @Override
    public MonitoringMetrics collectMetrics(String job) {
        /*
          CPU 사용률 / Heap 메모리 / 사용률 최근 5분간 4xx/5xx 에러 건수 / 최근 5분간 평균 응답 시간 메트릭 수집
         */
        try {
            Double cpu = querySingle(PromQLQueries.cpu(job));
            Double heap = querySingle(PromQLQueries.heap(job));
            Double errorCount = querySingle(PromQLQueries.httpErrorRate(job));
            Double avgRt = querySingle(PromQLQueries.avgResponseTime(job));

            return new MonitoringMetrics(job, cpu, heap, errorCount, avgRt);
        } catch (Exception e) {
            log.warn("Prometheus 메트릭 수집 실패 job={}: {}", job, e.getMessage());
            return MonitoringMetrics.empty(job);
        }
    }

    private Double querySingle(String promql) {
        PrometheusQueryResponse response = restClient.get()
                .uri(prometheusUrl + QUERY_PATH + "?query={query}", promql)
                .retrieve()
                .body(PrometheusQueryResponse.class);

        if (response == null || !"success".equals(response.status())) return null;
        List<Result> results = response.data().result();
        if (results == null || results.isEmpty()) return null;

        List<Object> value = results.get(0).value();
        if (value == null || value.size() < 2) return null;

        try {
            return Double.parseDouble(value.get(1).toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record PrometheusQueryResponse(String status, Data data) {}
    private record Data(String resultType, List<Result> result) {}
    private record Result(Map<String, String> metric, List<Object> value) {}
}