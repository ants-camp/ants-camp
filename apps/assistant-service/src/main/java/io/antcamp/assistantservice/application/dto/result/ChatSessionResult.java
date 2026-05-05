package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.ChatSession;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatSessionResult(UUID chatSessionId, UUID userId, String title,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static ChatSessionResult from(ChatSession session) {
        return new ChatSessionResult(
                session.getChatSessionId(),
                session.getUserId(),
                session.getTitle(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}