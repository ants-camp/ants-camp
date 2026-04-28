package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

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

    public static ChatMessage createUserMessage(UUID chatSessionId, String content, int seq) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("메시지 내용은 비어있을 수 없습니다.");
        return ChatMessage.builder()
                .chatMessageId(UUID.randomUUID())
                .chatSessionId(chatSessionId)
                .content(content)
                .role(Role.USER)
                .seq(seq)
                .sources(List.of())
                .build();
    }

    public static ChatMessage createBotMessage(UUID chatSessionId, String content, int seq, List<SourceReference> sources) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("메시지 내용은 비어있을 수 없습니다.");
        return ChatMessage.builder()
                .chatMessageId(UUID.randomUUID())
                .chatSessionId(chatSessionId)
                .content(content)
                .role(Role.BOT)
                .seq(seq)
                .sources(sources != null ? sources : List.of())
                .build();
    }

    public static ChatMessage restore(UUID chatMessageId, UUID chatSessionId, String content,
                                      Role role, int seq, List<SourceReference> sources) {
        return ChatMessage.builder()
                .chatMessageId(chatMessageId)
                .chatSessionId(chatSessionId)
                .content(content)
                .role(role)
                .seq(seq)
                .sources(sources != null ? sources : List.of())
                .build();
    }
}