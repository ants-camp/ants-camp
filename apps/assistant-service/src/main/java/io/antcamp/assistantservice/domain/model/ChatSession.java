package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ChatSession {

    private UUID chatSessionId;
    private UUID userId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatSession create(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        return ChatSession.builder()
                .userId(userId)
                .build();
    }

    public static ChatSession restore(UUID chatSessionId, UUID userId, String title,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        return ChatSession.builder()
                .chatSessionId(chatSessionId)
                .userId(userId)
                .title(title)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void updateTitle(String title) {
        this.title = (title != null && title.length() > 50) ? title.substring(0, 50) : title;
    }
}