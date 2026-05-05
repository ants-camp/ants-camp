package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.Role;
import io.antcamp.assistantservice.domain.model.SourceReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatMessageResult(UUID chatMessageId, Role role, String content,
                                int seq, List<SourceReference> sources, LocalDateTime createdAt) {

    public static ChatMessageResult from(ChatMessage message) {
        return new ChatMessageResult(
                message.getChatMessageId(),
                message.getRole(),
                message.getContent(),
                message.getSeq(),
                message.getSources(),
                message.getCreatedAt()
        );
    }
}