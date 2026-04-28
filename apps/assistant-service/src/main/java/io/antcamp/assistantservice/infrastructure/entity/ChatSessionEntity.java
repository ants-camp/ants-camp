package io.antcamp.assistantservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.ChatSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "p_chat_sessions")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSessionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chat_session_id", updatable = false, nullable = false)
    private UUID chatSessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public static ChatSessionEntity from(ChatSession domain) {
        return ChatSessionEntity.builder()
                .chatSessionId(domain.getChatSessionId())
                .userId(domain.getUserId())
                .build();
    }

    public ChatSession toDomain() {
        return ChatSession.restore(this.chatSessionId, this.userId);
    }
}