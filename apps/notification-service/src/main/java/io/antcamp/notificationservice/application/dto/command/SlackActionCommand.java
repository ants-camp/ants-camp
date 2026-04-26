package io.antcamp.notificationservice.application.dto.command;

import io.antcamp.notificationservice.domain.model.ResolutionAction;

import java.util.UUID;

public record SlackActionCommand(
        UUID notificationId,
        String slackUserId,
        ResolutionAction action
) {}