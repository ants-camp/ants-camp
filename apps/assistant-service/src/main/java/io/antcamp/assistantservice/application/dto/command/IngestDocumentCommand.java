package io.antcamp.assistantservice.application.dto.command;

import io.antcamp.assistantservice.domain.exception.InvalidDocumentException;
import io.antcamp.assistantservice.domain.model.DocType;

public record IngestDocumentCommand(
        String title,
        DocType type,
        String content
) {

    public IngestDocumentCommand {
        if (title == null || title.isBlank()) throw InvalidDocumentException.titleBlank();
        if (title.length() > 100) throw InvalidDocumentException.titleTooLong();
        if (type == null) throw InvalidDocumentException.typeNull();
        if (content == null || content.isBlank()) throw InvalidDocumentException.contentBlank();
    }
}