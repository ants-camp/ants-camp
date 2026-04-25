package io.antcamp.notificationservice.infrastructure.client.llm;

import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.domain.model.MonitoringMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Map.entry;

@Slf4j
@Component
public class PromptUtil {

    private final Resource promptResource;

    public PromptUtil(
            @Value("classpath:prompts/alert-analysis.st") Resource promptResource
    ) {
        this.promptResource = promptResource;
    }

    public String buildPromptPublic(PrometheusAlertCommand.AlertItem alert, MonitoringMetrics metrics, String recentLogs) {
        String template = loadTemplate();
        String firedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return render(template, Map.ofEntries(
                entry("alertName",       nullSafe(alert.alertName())),
                entry("firedAt",         firedAt),
                entry("severity",        nullSafe(alert.severity())),
                entry("job",             nullSafe(alert.job())),
                entry("instance",        nullSafe(alert.instance())),
                entry("uri",             nullSafe(alert.uri())),
                entry("httpStatus",      nullSafe(alert.httpStatus())),
                entry("exception",       nullSafe(alert.exception())),
                entry("summary",         nullSafe(alert.summary())),
                entry("description",     nullSafe(alert.description())),
                entry("cpu",             metrics.formatCpu()),
                entry("heap",            metrics.formatHeap()),
                entry("errorCount",      metrics.formatErrorCount()),
                entry("avgResponseTime", metrics.formatAvgResponseTime()),
                entry("recentLogs", recentLogs != null ? recentLogs : "수집된 로그 없음")
        ));
    }

    private String loadTemplate() {
        try {
            return promptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("프롬프트 템플릿 로드 실패: {}", e.getMessage());
            return "";
        }
    }

    private String render(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private String nullSafe(String value) {
        return value != null ? value : "-";
    }
}
