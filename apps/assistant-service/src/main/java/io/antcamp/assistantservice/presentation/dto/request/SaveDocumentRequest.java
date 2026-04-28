package io.antcamp.assistantservice.presentation.dto.request;

import io.antcamp.assistantservice.domain.model.DocType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveDocumentRequest(
        @NotBlank @Size(max = 100) String title,
        @NotNull DocType type,
        @NotBlank String content
) {
}
