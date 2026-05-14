package io.antcamp.assistantservice.application.dto.command;

import io.antcamp.assistantservice.domain.exception.InvalidInputException;
import io.antcamp.assistantservice.domain.exception.InvalidMessageContentException;
import io.antcamp.assistantservice.domain.exception.MessageTooLongException;

import java.util.UUID;

public record SendMessageCommand(
        UUID chatSessionId,
        UUID userId,
        String content
) {

    public SendMessageCommand {
        if (chatSessionId == null) throw new InvalidInputException();
        if (userId == null) throw new InvalidInputException();
        if (content == null || content.isBlank()) throw new InvalidMessageContentException();
        if (content.length() > 2000) throw new MessageTooLongException();
    }
}