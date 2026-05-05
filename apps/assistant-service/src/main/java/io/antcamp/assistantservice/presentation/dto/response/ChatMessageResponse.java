package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.ChatMessageResult;
import io.antcamp.assistantservice.domain.model.Role;
import io.antcamp.assistantservice.domain.model.SourceReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatMessageResponse(UUID chatMessageId, String role, String content,
                                  int seq, List<SourceReference> sources, LocalDateTime createdAt) {

    public static ChatMessageResponse from(ChatMessageResult result) {
        return new ChatMessageResponse(
                result.chatMessageId(),
                result.role().name(),
                result.content(),
                result.seq(),
                result.role() == Role.USER ? null : result.sources(),
                result.createdAt()
        );
    }
}