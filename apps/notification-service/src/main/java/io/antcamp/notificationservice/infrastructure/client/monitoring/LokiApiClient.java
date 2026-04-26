package io.antcamp.notificationservice.infrastructure.client.monitoring;

import io.antcamp.notificationservice.application.port.LogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@Component
@RequiredArgsConstructor
public class LokiApiClient implements LogPort {

    private static final String QUERY_RANGE_PATH = "/loki/api/v1/query_range";
    private static final int LOG_LIMIT = 20;            // 최대 20줄 반환
    private static final int LOOK_BACK_SECONDS = 600;   // 10분

    private final RestClient restClient;

    @Value("${loki.url}")
    private String lokiUrl;

    @Override
    public String collectRecentLogs(String job) {
        try {
            long endNano   = Instant.now().toEpochMilli() * 1_000_000L;
            long startNano = (Instant.now().toEpochMilli() - LOOK_BACK_SECONDS * 1000L) * 1_000_000L;

            String query = String.format("{job=\"%s\"} |~ \"ERROR|Exception\"", job);

            LokiQueryResponse response = restClient.get()
                    .uri(lokiUrl + QUERY_RANGE_PATH + "?query={query}&start={start}&end={end}&limit={limit}",
                            query, startNano, endNano, LOG_LIMIT)
                    .retrieve()
                    .body(LokiQueryResponse.class);

            if (response == null || response.safeResults().isEmpty()) {
                return null;
            }

            StringJoiner joiner = new StringJoiner("\n");
            for (StreamResult stream : response.safeResults()) {
                if (stream.values() == null) continue;
                for (List<String> entry : stream.values()) {
                    if (entry.size() >= 2) {
                        long ts = Long.parseLong(entry.get(0)) / 1_000_000; // 나노초 → 밀리초
                        String time = Instant.ofEpochMilli(ts).toString();
                        joiner.add(time + " " + entry.get(1));
                    }
                }
            }

            String logs = joiner.toString().trim();
            return logs.isEmpty() ? null : logs;

        } catch (Exception e) {
            log.warn("Loki 로그 수집 실패 job={}: {}", job, e.getMessage());
            return null;
        }
    }

    private record LokiQueryResponse(String status, Data data) {
        List<StreamResult> safeResults() {
            return data != null && data.result() != null ? data.result() : List.of();
        }
    }
    private record Data(String resultType, List<StreamResult> result) {}
    private record StreamResult(Map<String, String> stream, List<List<String>> values) {}
}