package io.antcamp.assistantservice.application.port;

import io.antcamp.assistantservice.domain.model.EvalScores;
import io.antcamp.assistantservice.domain.model.Verdict;

public interface JudgeLlmPort {

    record JudgeEvalResult(EvalScores scores, int latencyMs, int promptTokens, int completionTokens) {}

    /**
     * Reference-free 또는 Reference-based 채점
     * referenceAnswer가 null이면 Reference-free, 있으면 Reference-based 프롬프트 사용
     */
    JudgeEvalResult evaluate(String judgeModel, String question, String llmResponse,
                             String retrievedContext, String referenceAnswer);

    /**
     * Pairwise 비교 — 두 응답 중 어느 쪽이 더 나은지 판정
     */
    Verdict compare(String judgeModel, String question, String responseA, String responseB);
}