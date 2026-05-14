package io.antcamp.notificationservice.infrastructure.client.slack;

import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.model.ResolutionAction;
import io.antcamp.notificationservice.infrastructure.config.NotificationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackBlockBuilder {

    private final NotificationProperties properties;
    private final String grafanaUrl;

    public SlackBlockBuilder(NotificationProperties properties,
                             @Value("${grafana.url}") String grafanaUrl) {
        this.properties = properties;
        this.grafanaUrl = grafanaUrl;
    }

    public List<Map<String, Object>> buildAlertBlocks(Notification notification) {
        List<Map<String, Object>> blocks = buildBaseBlocks(notification);
        // Grafana 딥링크는 인프라 잡 포함 모든 알림에 노출
        blocks.add(grafanaLinkActions(notification.getJob(),
                notification.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant()));
        if (hasAiAnalysis(notification) && !properties.infrastructureJobs().contains(notification.getJob())) {
            blocks.add(actionsBlock(notification.getNotificationId().toString()));
        }
        return blocks;
    }

    public List<Map<String, Object>> buildProcessingBlocks(Notification notification, String handlerSlackUserId, ResolutionAction action) {
        List<Map<String, Object>> blocks = buildBaseBlocks(notification);
        blocks.add(grafanaLinkActions(notification.getJob(),
                notification.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant()));
        blocks.add(sectionBlock(String.format("⏳ *<@%s>* 님이 *%s* 처리를 진행 중입니다…", handlerSlackUserId, action.displayName())));
        return blocks;
    }

    public List<Map<String, Object>> buildHandledBlocks(Notification notification, String handlerSlackUserId, ResolutionAction action, boolean succeeded) {
        List<Map<String, Object>> blocks = buildBaseBlocks(notification);
        blocks.add(grafanaLinkActions(notification.getJob(),
                notification.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant()));
        String statusText = succeeded
                ? String.format("✅ *<@%s>* 님이 *%s* 처리하였습니다.", handlerSlackUserId, action.displayName())
                : String.format("❌ *<@%s>* 님이 *%s* 시도하였으나 실패하였습니다.", handlerSlackUserId, action.displayName());
        blocks.add(sectionBlock(statusText));
        return blocks;
    }

    private List<Map<String, Object>> buildBaseBlocks(Notification notification) {
        List<Map<String, Object>> blocks = new ArrayList<>();

        String headerText = String.format("🚨 [%s] %s", notification.getSeverity(), notification.getTitle());
        blocks.add(headerBlock(headerText.length() > 150 ? headerText.substring(0, 147) + "..." : headerText));
        blocks.add(sectionBlock(notification.getContent()));
        blocks.add(dividerBlock());

        if (hasAiAnalysis(notification)) {
            String analysis = notification.getAiAnalysis();
            if (analysis.length() > 2900) analysis = analysis.substring(0, 2900) + "...";
            blocks.add(sectionBlock("*🤖 AI 장애 분석*\n" + analysis));
            blocks.add(dividerBlock());
        }

        return blocks;
    }

    private boolean hasAiAnalysis(Notification notification) {
        return notification.getAiAnalysis() != null && !notification.getAiAnalysis().isBlank();
    }

    private Map<String, Object> headerBlock(String text) {
        return Map.of("type", "header", "text", Map.of("type", "plain_text", "text", text));
    }

    private Map<String, Object> sectionBlock(String text) {
        return Map.of("type", "section", "text", Map.of("type", "mrkdwn", "text", text));
    }

    private Map<String, Object> dividerBlock() {
        return Map.of("type", "divider");
    }

    private Map<String, Object> actionsBlock(String notificationId) {
        return Map.of(
                "type", "actions",
                "block_id", "alert_actions_" + notificationId,
                "elements", List.of(
                        buttonElement("🔄 코드 롤백",    "action_rollback",    "ROLLBACK",    "danger"),
                        buttonElement("🗑️ 캐시 비우기",  "action_cache_clear", "CACHE_CLEAR", null),
                        buttonElement("🔁 서비스 재시작", "action_restart",     "RESTART",     null),
                        buttonElement("✅ 정상 처리",    "action_false_alarm", "FALSE_ALARM", "primary")
                )
        );
    }

    private Map<String, Object> buttonElement(String label, String actionId, String value, String style) {
        Map<String, Object> button = new HashMap<>();
        button.put("type", "button");
        button.put("text", Map.of("type", "plain_text", "text", label));
        button.put("action_id", actionId);
        button.put("value", value);
        if (style != null) button.put("style", style);
        return button;
    }

    private Map<String, Object> grafanaLinkActions(String job, Instant alertTime) {
        long from = alertTime.minus(Duration.ofMinutes(10)).toEpochMilli();
        long to   = alertTime.plus(Duration.ofMinutes(5)).toEpochMilli();
        String timeParams   = "?var-job=%s&from=%d&to=%d".formatted(urlEnc(job), from, to);
        String dashboardUrl = grafanaUrl + "/d/service-dashboard/service-dashboard" + timeParams;
        String logsUrl      = grafanaUrl + "/d/logs-dashboard/logs-dashboard" + timeParams;
        return Map.of(
                "type", "actions",
                "elements", List.of(
                        linkButton("📊 Grafana 대시보드", dashboardUrl),
                        linkButton("📋 로그 보기", logsUrl)
                )
        );
    }

    private Map<String, Object> linkButton(String label, String url) {
        return Map.of(
                "type", "button",
                "text", Map.of("type", "plain_text", "text", label),
                "url", url
        );
    }

    private String urlEnc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}