package io.antcamp.notificationservice.infrastructure.client.llm;

import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.domain.exception.PromptTemplateLoadFailedException;
import io.antcamp.notificationservice.domain.model.MonitoringMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Map.entry;

@Slf4j
@Component
public class PromptUtil {

    private static final int MAX_LOG_LENGTH = 4000;
    private static final DateTimeFormatter KST_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Seoul"));
    //토큰, 비밀번호, 이메일 같은 값이 외부 API로 전송됨을 방지
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(authorization|token|password|secret|bearer|credential|api-?key)[\\s]*[:=][\\s]*\\S+");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private final String template;

    public PromptUtil(
            @Value("classpath:prompts/alert-analysis.st") Resource promptResource
    ) {
        try {
            this.template = promptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("프롬프트 템플릿 로드 실패", e);
            throw new PromptTemplateLoadFailedException();
        }
    }

    public String buildPromptPublic(PrometheusAlertCommand.AlertItem alert, MonitoringMetrics metrics, String recentLogs) {
        return render(template, Map.ofEntries(
                entry("alertName",       nullSafe(alert.alertName())),
                entry("firedAt",         toKst(alert.startsAt())),
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
                entry("recentLogs",      sanitizeLogs(recentLogs))
        ));
    }

    private String sanitizeLogs(String logs) {
        if (logs == null) return "수집된 로그 없음";
        String masked = SENSITIVE_PATTERN.matcher(logs).replaceAll("$1=[REDACTED]");
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("[EMAIL]");
        if (masked.length() > MAX_LOG_LENGTH) {
            masked = masked.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
        }
        return masked;
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

    private String toKst(String isoUtc) {
        if (isoUtc == null || isoUtc.isBlank()) return "-";
        try {
            return KST_FORMATTER.format(Instant.parse(isoUtc)) + " KST";
        } catch (Exception e) {
            return isoUtc;
        }
    }
}
