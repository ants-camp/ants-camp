package io.antcamp.assistantservice.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record RunEvaluationRequest(
        @NotEmpty(message = "평가 질문은 최소 1개 이상이어야 합니다.")
        List<EvalQuestionRequest> questions,

        @NotEmpty(message = "평가 모델은 최소 1개 이상이어야 합니다.")
        List<String> judgeModels,

        UUID promptVersionId, // 선택 — null이면 기본 프롬프트 사용
        String memo           // 선택 — 변동 요인 메모
) {}