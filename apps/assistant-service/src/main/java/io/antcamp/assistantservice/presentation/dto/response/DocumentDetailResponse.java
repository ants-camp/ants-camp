package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.DocumentDetailResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentDetailResponse(
        UUID documentId,
        String title,
        String type,
        String content,
        int chunkCount,
        String ingestStatus,
        String failureReason,
        LocalDateTime createdAt
) {

    public static DocumentDetailResponse from(DocumentDetailResult result) {
        return new DocumentDetailResponse(
                result.documentId(), result.title(), result.type(), result.content(), result.chunkCount(),
                result.ingestStatus(), result.failureReason(), result.createdAt()
        );
    }
}