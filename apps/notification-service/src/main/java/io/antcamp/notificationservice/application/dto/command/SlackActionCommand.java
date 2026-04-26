package io.antcamp.notificationservice.application.dto.command;

import io.antcamp.notificationservice.domain.model.ResolutionAction;

import java.util.UUID;

public record SlackActionCommand(
        UUID notificationId,
        String slackUserId,
        ResolutionAction action
) {
    public SlackActionCommand {
        if (notificationId == null) throw new IllegalArgumentException("notificationId는 필수입니다.");
        if (slackUserId == null || slackUserId.isBlank()) throw new IllegalArgumentException("slackUserId는 필수입니다.");
        if (action == null) throw new IllegalArgumentException("action은 필수입니다.");
    }

    public static SlackActionCommand of(UUID notificationId, String slackUserId, String actionValue) {
        return new SlackActionCommand(notificationId, slackUserId, ResolutionAction.valueOf(actionValue));
    }
}