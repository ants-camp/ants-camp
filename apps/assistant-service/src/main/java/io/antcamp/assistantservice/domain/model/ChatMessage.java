package io.antcamp.assistantservice.domain.model;

import io.antcamp.assistantservice.domain.exception.InvalidMessageContentException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ChatMessage {

    private UUID chatMessageId;
    private UUID chatSessionId;
    private String content;
    private Role role;
    private int seq;
    private List<SourceReference> sources;
    private MessageStatus status;
    private LocalDateTime createdAt;

    public static ChatMessage createUserMessage(UUID chatSessionId, String content, int seq) {
        if (content == null || content.isBlank()) throw new InvalidMessageContentException();
        return ChatMessage.builder()
                .chatSessionId(chatSessionId)
                .content(content)
                .role(Role.USER)
                .seq(seq)
                .sources(List.of())
                .status(MessageStatus.PENDING)
                .build();
    }

    public static ChatMessage createBotMessage(UUID chatSessionId, String content, int seq, List<SourceReference> sources) {
        if (content == null || content.isBlank()) throw new InvalidMessageContentException();
        return ChatMessage.builder()
                .chatSessionId(chatSessionId)
                .content(content)
                .role(Role.BOT)
                .seq(seq)
                .sources(sources != null ? sources : List.of())
                .status(MessageStatus.COMPLETED)
                .build();
    }

    public void complete() {
        this.status = MessageStatus.COMPLETED;
    }

    public static ChatMessage restore(UUID chatMessageId, UUID chatSessionId, String content,
                                      Role role, int seq, List<SourceReference> sources,
                                      MessageStatus status, LocalDateTime createdAt) {
        return ChatMessage.builder()
                .chatMessageId(chatMessageId)
                .chatSessionId(chatSessionId)
                .content(content)
                .role(role)
                .seq(seq)
                .sources(sources != null ? sources : List.of())
                .status(status)
                .createdAt(createdAt)
                .build();
    }
}