package io.antcamp.assistantservice.application.dto.command;

import io.antcamp.assistantservice.domain.exception.InvalidDocumentException;
import io.antcamp.assistantservice.domain.exception.InvalidInputException;
import io.antcamp.assistantservice.domain.model.DocType;

import java.util.UUID;

public record UpdateDocumentCommand(
        UUID documentId,
        String title,
        DocType type,
        String content
) {

    public UpdateDocumentCommand {
        if (documentId == null) throw new InvalidInputException();
        if (title == null || title.isBlank()) throw InvalidDocumentException.titleBlank();
        if (title.length() > 100) throw InvalidDocumentException.titleTooLong();
        if (type == null) throw InvalidDocumentException.typeNull();
        if (content == null || content.isBlank()) throw InvalidDocumentException.contentBlank();
    }
}