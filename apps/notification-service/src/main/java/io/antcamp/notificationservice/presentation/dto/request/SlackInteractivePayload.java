package io.antcamp.notificationservice.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SlackInteractivePayload(
        String type,
        User user,
        List<Action> actions
) {
    private static final String BLOCK_ID_PREFIX = "alert_actions_";
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(String id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Action(
            @JsonProperty("action_id") String actionId,
            @JsonProperty("block_id") String blockId,
            String value
    ) {}

    public String slackUserId() {
        return user != null ? user.id() : null;
    }

    public Action firstAction() {
        return actions != null && !actions.isEmpty() ? actions.get(0) : null;
    }

    public UUID notificationId() {
        Action action = firstAction();
        if (action == null || action.blockId() == null) return null;
        try {
            return UUID.fromString(action.blockId().replace(BLOCK_ID_PREFIX, ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}