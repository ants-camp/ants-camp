package io.antcamp.assistantservice.application.dto.command;

import io.antcamp.assistantservice.domain.model.DocType;

import java.util.Objects;
import java.util.UUID;

public record UpdateDocumentCommand(
        UUID documentId,
        String title,
        DocType type,
        String content
) {

    public UpdateDocumentCommand {
        Objects.requireNonNull(documentId, "문서 ID는 필수입니다.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("문서 제목은 비어있을 수 없습니다.");
        if (title.length() > 100) throw new IllegalArgumentException("문서 제목은 100자를 초과할 수 없습니다.");
        Objects.requireNonNull(type, "문서 타입은 필수입니다.");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("문서 내용은 비어있을 수 없습니다.");
    }
}