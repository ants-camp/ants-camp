package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.domain.model.PairwiseRun;
import io.antcamp.assistantservice.domain.model.PairwiseSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PairwiseSummaryResponse(
        UUID evalRunIdA,
        String ragModelA,
        UUID evalRunIdB,
        String ragModelB,
        String status,
        int totalCount,
        int doneCount,
        List<JudgeSummaryResponse> byJudge
) {
    public record JudgeSummaryResponse(
            String judgeModel,
            long aWins,
            long bWins,
            long ties,
            long total
    ) {}

    public static PairwiseSummaryResponse from(PairwiseSummary summary, String ragModelA, String ragModelB,
                                               Optional<PairwiseRun> run) {
        List<JudgeSummaryResponse> byJudge = summary.byJudge().stream()
                .map(j -> new JudgeSummaryResponse(
                        j.judgeModel(), j.aWins(), j.bWins(), j.ties(), j.total()))
                .toList();
        return new PairwiseSummaryResponse(
                summary.evalRunIdA(), ragModelA,
                summary.evalRunIdB(), ragModelB,
                run.map(r -> r.getStatus().name()).orElse(null),
                run.map(PairwiseRun::getTotalCount).orElse(0),
                run.map(PairwiseRun::getDoneCount).orElse(0),
                byJudge);
    }
}