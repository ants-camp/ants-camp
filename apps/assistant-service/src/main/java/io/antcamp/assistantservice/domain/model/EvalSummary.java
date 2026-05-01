package io.antcamp.assistantservice.domain.model;

import java.util.List;

public record EvalSummary(
        List<JudgeSummary> byJudge,        // Judge별 평균
        double avgRelevance,                // 전체 평균 (모든 Judge 포함)
        double avgFaithfulness,
        double avgContextPrecision,
        long hallucinationCount
) {
    public record JudgeSummary(
            String judgeModel,
            double avgRelevance,
            double avgFaithfulness,
            double avgContextPrecision,
            long count
    ) {}
}