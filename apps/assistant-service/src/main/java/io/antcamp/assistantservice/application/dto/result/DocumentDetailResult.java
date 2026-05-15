package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.KnowledgeDocument;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentDetailResult(UUID documentId, String title, String type, String content, int chunkCount,
                                    String ingestStatus, String failureReason, LocalDateTime createdAt) {

    public static DocumentDetailResult from(KnowledgeDocument document, int chunkCount) {
        return new DocumentDetailResult(
                document.getDocumentId(),
                document.getTitle(),
                document.getType().name(),
                document.getContent(),
                chunkCount,
                document.getIngestStatus().name(),
                document.getFailureReason(),
                document.getCreatedAt()
        );
    }
}