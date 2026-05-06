package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.SendMessageResult;
import io.antcamp.assistantservice.domain.model.SourceReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SendMessageResponse(UUID chatMessageId, String role, String content,
                                  int seq, List<SourceReference> sources, LocalDateTime createdAt) {

    public static SendMessageResponse from(SendMessageResult result) {
        return new SendMessageResponse(
                result.chatMessageId(),
                "BOT",
                result.content(),
                result.seq(),
                result.sources(),
                result.createdAt()
        );
    }
}