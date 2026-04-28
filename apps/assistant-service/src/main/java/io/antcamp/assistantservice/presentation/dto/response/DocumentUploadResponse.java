package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.DocumentUploadResult;

import java.util.UUID;

public record DocumentUploadResponse(
        UUID documentId,
        String title,
        String type
) {

    public static DocumentUploadResponse from(DocumentUploadResult result) {
        return new DocumentUploadResponse(result.documentId(), result.title(), result.type());
    }
}