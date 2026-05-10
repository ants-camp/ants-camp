package io.antcamp.notificationservice.application.dto.command;

import io.antcamp.notificationservice.domain.exception.InvalidInputException;
import io.antcamp.notificationservice.domain.model.ResolutionAction;

import java.util.UUID;

public record SlackActionCommand(
        UUID notificationId,
        String slackUserId,
        ResolutionAction action
) {
    public SlackActionCommand {
        if (notificationId == null) throw new InvalidInputException();
        if (slackUserId == null || slackUserId.isBlank()) throw new InvalidInputException();
        if (action == null) throw new InvalidInputException();
    }

    public static SlackActionCommand of(UUID notificationId, String slackUserId, String actionValue) {
        return new SlackActionCommand(notificationId, slackUserId, ResolutionAction.valueOf(actionValue));
    }
}