package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.KnowledgeDocument;

import java.util.UUID;

public record DocumentUploadResult(UUID documentId, String title, String type) {

    public static DocumentUploadResult from(KnowledgeDocument document) {
        return new DocumentUploadResult(
                document.getDocumentId(),
                document.getTitle(),
                document.getType().name()
        );
    }
}