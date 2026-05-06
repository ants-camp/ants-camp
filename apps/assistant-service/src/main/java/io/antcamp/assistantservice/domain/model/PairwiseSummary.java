package io.antcamp.assistantservice.domain.model;

import java.util.List;
import java.util.UUID;

public record PairwiseSummary(
        UUID evalRunIdA,
        UUID evalRunIdB,
        List<JudgeSummary> byJudge
) {
    public record JudgeSummary(String judgeModel, long aWins, long bWins, long ties, long total) {}
}