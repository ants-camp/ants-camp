package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.SourceReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SendMessageResult(UUID chatMessageId, String content, int seq,
                                List<SourceReference> sources, LocalDateTime createdAt) {

    public static SendMessageResult from(ChatMessage botMessage) {
        return new SendMessageResult(
                botMessage.getChatMessageId(),
                botMessage.getContent(),
                botMessage.getSeq(),
                botMessage.getSources(),
                botMessage.getCreatedAt()
        );
    }
}