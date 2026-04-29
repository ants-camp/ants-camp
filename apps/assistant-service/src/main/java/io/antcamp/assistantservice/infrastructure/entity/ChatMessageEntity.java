package io.antcamp.assistantservice.infrastructure.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.MessageStatus;
import io.antcamp.assistantservice.domain.model.Role;
import io.antcamp.assistantservice.domain.model.SourceReference;
import io.antcamp.assistantservice.infrastructure.util.JsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_chat_messages")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chat_message_id", updatable = false, nullable = false)
    private UUID chatMessageId;

    @Column(name = "chat_session_id", nullable = false)
    private UUID chatSessionId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "seq", nullable = false)
    private int seq;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sources", nullable = false, columnDefinition = "jsonb")
    private String sources;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status;

    public static ChatMessageEntity from(ChatMessage domain) {
        return ChatMessageEntity.builder()
                .chatMessageId(domain.getChatMessageId())
                .chatSessionId(domain.getChatSessionId())
                .content(domain.getContent())
                .role(domain.getRole())
                .seq(domain.getSeq())
                .sources(JsonConverter.toJson(domain.getSources()))
                .status(domain.getStatus())
                .build();
    }

    public ChatMessage toDomain() {
        List<SourceReference> sourceList = JsonConverter.fromJson(
                this.sources, new TypeReference<>() {}
        );
        return ChatMessage.restore(
                this.chatMessageId, this.chatSessionId, this.content,
                this.role, this.seq, sourceList, this.status, this.getCreatedAt()
        );
    }
}