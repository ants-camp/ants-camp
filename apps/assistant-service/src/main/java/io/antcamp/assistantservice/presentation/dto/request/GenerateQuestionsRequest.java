package io.antcamp.assistantservice.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GenerateQuestionsRequest(
        @Min(value = 1, message = "질문 수는 1개 이상이어야 합니다.")
        @Max(value = 50, message = "질문 수는 50개를 초과할 수 없습니다.")
        int count
) {}