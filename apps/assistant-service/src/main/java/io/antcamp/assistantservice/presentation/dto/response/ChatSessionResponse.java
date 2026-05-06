package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.ChatSessionResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatSessionResponse(UUID chatSessionId, UUID userId, String title,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static ChatSessionResponse from(ChatSessionResult result) {
        return new ChatSessionResponse(
                result.chatSessionId(),
                result.userId(),
                result.title(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}