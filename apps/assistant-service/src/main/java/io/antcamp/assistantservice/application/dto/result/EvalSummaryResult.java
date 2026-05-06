package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.EvalSummary;

import java.util.List;

public record EvalSummaryResult(
        List<JudgeSummaryResult> byJudge,
        double avgRelevance,
        double avgFaithfulness,
        double avgContextPrecision,
        long hallucinationCount
) {
    public record JudgeSummaryResult(
            String judgeModel,
            double avgRelevance,
            double avgFaithfulness,
            double avgContextPrecision,
            long count
    ) {}

    public static EvalSummaryResult from(EvalSummary summary) {
        List<JudgeSummaryResult> byJudge = summary.byJudge().stream()
                .map(j -> new JudgeSummaryResult(
                        j.judgeModel(), j.avgRelevance(), j.avgFaithfulness(),
                        j.avgContextPrecision(), j.count()))
                .toList();
        return new EvalSummaryResult(byJudge, summary.avgRelevance(),
                summary.avgFaithfulness(), summary.avgContextPrecision(), summary.hallucinationCount());
    }
}