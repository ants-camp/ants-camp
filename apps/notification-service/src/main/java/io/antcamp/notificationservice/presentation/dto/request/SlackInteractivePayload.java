package io.antcamp.notificationservice.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SlackInteractivePayload(
        String type,
        User user,
        List<Action> actions
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(String id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Action(
            String action_id,
            String block_id,
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
        if (action == null || action.block_id() == null) return null;
        try {
            return UUID.fromString(action.block_id().replace("alert_actions_", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}