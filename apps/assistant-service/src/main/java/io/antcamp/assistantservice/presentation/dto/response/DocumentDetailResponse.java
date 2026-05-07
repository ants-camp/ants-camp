package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.DocumentDetailResult;

import java.util.UUID;

public record DocumentDetailResponse(
        UUID documentId,
        String title,
        String type,
        String content,
        int chunkCount
) {

    public static DocumentDetailResponse from(DocumentDetailResult result) {
        return new DocumentDetailResponse(
                result.documentId(), result.title(), result.type(), result.content(), result.chunkCount()
        );
    }
}