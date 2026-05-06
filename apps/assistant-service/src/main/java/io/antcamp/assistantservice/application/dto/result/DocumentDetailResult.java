package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.KnowledgeDocument;

import java.util.UUID;

public record DocumentDetailResult(UUID documentId, String title, String type, String content, int chunkCount) {

    public static DocumentDetailResult from(KnowledgeDocument document, int chunkCount) {
        return new DocumentDetailResult(
                document.getDocumentId(),
                document.getTitle(),
                document.getType().name(),
                document.getContent(),
                chunkCount
        );
    }
}