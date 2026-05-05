package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.domain.model.PromptVersion;

import java.util.UUID;

public record PromptVersionResponse(
        UUID promptVersionId,
        String name,
        String content
) {
    public static PromptVersionResponse from(PromptVersion domain) {
        return new PromptVersionResponse(
                domain.getPromptVersionId(),
                domain.getName(),
                domain.getContent()
        );
    }
}