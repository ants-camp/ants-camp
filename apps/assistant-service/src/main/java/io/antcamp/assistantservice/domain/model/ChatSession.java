package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ChatSession {

    private UUID chatSessionId;
    private UUID userId;

    public static ChatSession create(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        return ChatSession.builder()
                .chatSessionId(UUID.randomUUID())
                .userId(userId)
                .build();
    }

    public static ChatSession restore(UUID chatSessionId, UUID userId) {
        return ChatSession.builder()
                .chatSessionId(chatSessionId)
                .userId(userId)
                .build();
    }

}