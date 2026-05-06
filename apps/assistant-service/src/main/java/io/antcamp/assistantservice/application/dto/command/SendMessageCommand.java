package io.antcamp.assistantservice.application.dto.command;

import io.antcamp.assistantservice.domain.exception.MessageTooLongException;

import java.util.Objects;
import java.util.UUID;

public record SendMessageCommand(
        UUID chatSessionId,
        UUID userId,
        String content
) {

    public SendMessageCommand {
        Objects.requireNonNull(chatSessionId, "세션 ID는 필수입니다.");
        Objects.requireNonNull(userId, "사용자 ID는 필수입니다.");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("메시지 내용은 필수입니다.");
        if (content.length() > 2000) throw new MessageTooLongException();
    }
}