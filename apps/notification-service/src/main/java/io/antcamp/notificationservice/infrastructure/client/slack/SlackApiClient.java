package io.antcamp.notificationservice.infrastructure.client.slack;

import io.antcamp.notificationservice.application.port.ActionResult;
import io.antcamp.notificationservice.application.port.AlertPort;
import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.model.ResolutionAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackApiClient implements AlertPort {

    private static final String CHAT_POST_MESSAGE_URL = "https://slack.com/api/chat.postMessage";
    private static final String CHAT_UPDATE_URL = "https://slack.com/api/chat.update";

    private final RestClient restClient;
    private final SlackBlockBuilder blockBuilder;

    @Value("${slack.bot-token}")
    private String botToken;

    @Override
    public String send(Notification notification) {
        String title = String.format("🚨 [%s] %s", notification.getSeverity(), notification.getTitle());

        Map<String, Object> body = new HashMap<>();
        body.put("channel", notification.getChannelId());
        body.put("text", title);
        body.put("blocks", blockBuilder.buildAlertBlocks(notification));

        SlackResponse response = post(CHAT_POST_MESSAGE_URL, body);
        if (response == null || !response.ok()) {
            throw new RuntimeException("Slack 전송 실패: " + (response != null ? response.error() : "null response"));
        }

        log.info("Slack 전송 완료: title={}, ts={}", notification.getTitle(), response.ts());
        return response.ts();
    }

    @Override
    public void notifyActionResult(String channelId, String threadTs, ResolutionAction action, String slackUserId, ActionResult result) {
        sendThreadMessage(channelId, threadTs, SlackMessageFormatter.threadReplyText(action, slackUserId, result));
        log.info("스레드 답글 전송 완료: threadTs={}, succeeded={}", threadTs, result instanceof ActionResult.Success);
    }

    private void sendThreadMessage(String channelId, String threadTs, String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("channel", channelId);
        body.put("thread_ts", threadTs);
        body.put("text", text);

        SlackResponse response = post(CHAT_POST_MESSAGE_URL, body);
        if (response == null || !response.ok()) {
            throw new RuntimeException("스레드 답글 전송 실패: " + (response != null ? response.error() : "null response"));
        }
    }

    /**
     * 진행중
     */
    @Override
    public void markAsProcessing(Notification notification, String handlerSlackUserId, ResolutionAction action) {
        Map<String, Object> body = new HashMap<>();
        body.put("channel", notification.getChannelId());
        body.put("ts", notification.getSlackMessageTs());
        body.put("text", notification.getTitle());
        body.put("blocks", blockBuilder.buildProcessingBlocks(notification, handlerSlackUserId, action));

        SlackResponse response = post(CHAT_UPDATE_URL, body);
        if (response == null || !response.ok()) {
            log.warn("Slack '처리 중' 메시지 갱신 실패: {}", response != null ? response.error() : "null response");
        }
    }

    @Override
    public void markAsHandled(Notification notification, String handlerSlackUserId, ResolutionAction action, boolean succeeded) {
        Map<String, Object> body = new HashMap<>();
        body.put("channel", notification.getChannelId());
        body.put("ts", notification.getSlackMessageTs());
        body.put("text", notification.getTitle());
        body.put("blocks", blockBuilder.buildHandledBlocks(notification, handlerSlackUserId, action, succeeded));

        SlackResponse response = post(CHAT_UPDATE_URL, body);
        if (response == null || !response.ok()) {
            log.warn("Slack 메시지 업데이트 실패: {}", response != null ? response.error() : "null response");
        }
    }

    @Override
    public String getUserEmail(String slackUserId) {
        SlackUserResponse response = restClient.get()
                .uri("https://slack.com/api/users.info?user={userId}", slackUserId)
                .header("Authorization", "Bearer " + botToken)
                .retrieve()
                .body(SlackUserResponse.class);

        if (response == null || !response.ok() || response.user() == null) {
            log.warn("Slack 사용자 정보 조회 실패: slackUserId={}", slackUserId);
            return null;
        }
        return response.user().profile().email();
    }

    private SlackResponse post(String url, Map<String, Object> body) {
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + botToken)
                .body(body)
                .retrieve()
                .body(SlackResponse.class);
    }

    private record SlackResponse(boolean ok, String ts, String error) {}

    private record SlackUserResponse(boolean ok, SlackUser user) {
        private record SlackUser(SlackProfile profile) {}
        private record SlackProfile(String email) {}
    }
}