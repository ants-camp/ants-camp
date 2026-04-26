package io.antcamp.notificationservice.infrastructure.client.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.application.port.LlmPort;
import io.antcamp.notificationservice.domain.model.MonitoringMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Primary
@Component
public class ClaudeApiClient implements LlmPort {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    private final RestClient restClient;
    private final PromptUtil promptUtil;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaudeApiClient(
            RestClient restClient,
            PromptUtil promptUtil,
            @Value("${anthropic.api-key}") String apiKey
    ) {
        this.restClient = restClient;
        this.promptUtil = promptUtil;
        this.apiKey = apiKey;
    }

    @Override
    public String analyze(PrometheusAlertCommand.AlertItem alert, MonitoringMetrics metrics, String recentLogs) {
        String prompt = promptUtil.buildPromptPublic(alert, metrics, recentLogs);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", 2000,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String raw = restClient.post()
                    .uri(CLAUDE_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .body(body)
                    .exchange((req, res) -> {
                        byte[] bytes = res.getBody().readAllBytes();
                        return bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : null;
                    });

            if (raw == null || raw.isBlank()) return null;

            JsonNode root = objectMapper.readTree(raw);
            if (root.has("error")) {
                log.error("Claude API 오류 응답: {}", root.path("error").path("message").asText());
                return null;
            }

            JsonNode text = root.path("content").path(0).path("text");
            return text.isMissingNode() ? null : text.asText();

        } catch (Exception e) {
            log.error("Claude API 호출 실패: {}", e.getMessage());
            return null;
        }
    }
}