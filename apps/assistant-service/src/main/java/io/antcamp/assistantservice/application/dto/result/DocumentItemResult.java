package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.KnowledgeDocument;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentItemResult(UUID documentId, String title, String type, LocalDateTime updatedAt) {

    public static DocumentItemResult from(KnowledgeDocument document) {
        return new DocumentItemResult(
                document.getDocumentId(),
                document.getTitle(),
                document.getType().name(),
                document.getUpdatedAt()
        );
    }
}