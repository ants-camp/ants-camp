package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.domain.model.PairwiseSummary;

import java.util.List;
import java.util.UUID;

public record PairwiseSummaryResponse(
        UUID evalRunIdA,
        UUID evalRunIdB,
        List<JudgeSummaryResponse> byJudge
) {
    public record JudgeSummaryResponse(
            String judgeModel,
            long aWins,
            long bWins,
            long ties,
            long total
    ) {}

    public static PairwiseSummaryResponse from(PairwiseSummary summary) {
        List<JudgeSummaryResponse> byJudge = summary.byJudge().stream()
                .map(j -> new JudgeSummaryResponse(
                        j.judgeModel(), j.aWins(), j.bWins(), j.ties(), j.total()))
                .toList();
        return new PairwiseSummaryResponse(summary.evalRunIdA(), summary.evalRunIdB(), byJudge);
    }
}