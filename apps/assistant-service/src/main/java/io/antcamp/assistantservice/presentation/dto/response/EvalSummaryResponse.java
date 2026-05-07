package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.EvalSummaryResult;

import java.util.List;

public record EvalSummaryResponse(
        List<JudgeSummaryResponse> byJudge,
        double avgRelevance,
        double avgFaithfulness,
        double avgContextPrecision,
        long hallucinationCount
) {
    public record JudgeSummaryResponse(
            String judgeModel,
            double avgRelevance,
            double avgFaithfulness,
            double avgContextPrecision,
            long count
    ) {}

    public static EvalSummaryResponse from(EvalSummaryResult result) {
        List<JudgeSummaryResponse> byJudge = result.byJudge().stream()
                .map(j -> new JudgeSummaryResponse(
                        j.judgeModel(), j.avgRelevance(), j.avgFaithfulness(),
                        j.avgContextPrecision(), j.count()))
                .toList();
        return new EvalSummaryResponse(byJudge, result.avgRelevance(),
                result.avgFaithfulness(), result.avgContextPrecision(), result.hallucinationCount());
    }
}