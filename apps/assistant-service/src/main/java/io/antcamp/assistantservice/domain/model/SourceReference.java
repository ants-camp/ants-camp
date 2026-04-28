package io.antcamp.assistantservice.domain.model;

import java.util.Objects;
import java.util.UUID;

// chat_message.sources JSONB 항목
public record SourceReference(UUID knowledgeDocumentId, String title, String docType) {

    public SourceReference {
        Objects.requireNonNull(knowledgeDocumentId, "문서 ID는 필수입니다.");
        Objects.requireNonNull(title, "문서 제목은 필수입니다.");
        Objects.requireNonNull(docType, "문서 타입은 필수입니다.");
    }
}