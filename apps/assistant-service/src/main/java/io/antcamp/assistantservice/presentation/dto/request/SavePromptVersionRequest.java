package io.antcamp.assistantservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SavePromptVersionRequest(
        @NotBlank(message = "버전 이름은 비어있을 수 없습니다.")
        String name,

        @NotBlank(message = "프롬프트 내용은 비어있을 수 없습니다.")
        String content
) {}