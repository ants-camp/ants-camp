package io.antcamp.assistantservice.application.dto.command;

import common.exception.ErrorCode;
import io.antcamp.assistantservice.domain.exception.InvalidDocumentException;
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
        if (title == null || title.isBlank()) throw new InvalidDocumentException(ErrorCode.DOCUMENT_TITLE_BLANK);
        if (title.length() > 100) throw new InvalidDocumentException(ErrorCode.DOCUMENT_TITLE_TOO_LONG);
        Objects.requireNonNull(type, "문서 타입은 필수입니다.");
        if (content == null || content.isBlank()) throw new InvalidDocumentException(ErrorCode.DOCUMENT_CONTENT_BLANK);
    }
}