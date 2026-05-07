package io.antcamp.assistantservice.presentation.dto.request;

import io.antcamp.assistantservice.domain.model.EvalQuestion;
import jakarta.validation.constraints.NotBlank;

public record EvalQuestionRequest(
        @NotBlank(message = "질문은 비어있을 수 없습니다.")
        String question,
        String referenceAnswer  // 선택 — null이면 Reference-free 채점
) {
    public EvalQuestion toDomain() {
        return new EvalQuestion(question, referenceAnswer);
    }
}