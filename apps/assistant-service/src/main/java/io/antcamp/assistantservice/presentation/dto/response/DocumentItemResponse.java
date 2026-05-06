package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.DocumentItemResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentItemResponse(
        UUID documentId,
        String title,
        String type,
        LocalDateTime updatedAt
) {

    public static DocumentItemResponse from(DocumentItemResult result) {
        return new DocumentItemResponse(result.documentId(), result.title(), result.type(), result.updatedAt());
    }
}