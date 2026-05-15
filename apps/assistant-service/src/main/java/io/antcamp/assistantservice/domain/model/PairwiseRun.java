package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class PairwiseRun {

    private UUID pairwiseRunId;
    private UUID evalRunIdA;
    private UUID evalRunIdB;
    private PairwiseRunStatus status;
    private int totalCount;
    private int doneCount;

    public static PairwiseRun create(UUID evalRunIdA, UUID evalRunIdB) {
        return PairwiseRun.builder()
                .pairwiseRunId(UUID.randomUUID())
                .evalRunIdA(evalRunIdA)
                .evalRunIdB(evalRunIdB)
                .status(PairwiseRunStatus.PENDING)
                .totalCount(0)
                .doneCount(0)
                .build();
    }

    public static PairwiseRun restore(UUID pairwiseRunId, UUID evalRunIdA, UUID evalRunIdB,
                                       PairwiseRunStatus status, int totalCount, int doneCount) {
        return PairwiseRun.builder()
                .pairwiseRunId(pairwiseRunId)
                .evalRunIdA(evalRunIdA)
                .evalRunIdB(evalRunIdB)
                .status(status)
                .totalCount(totalCount)
                .doneCount(doneCount)
                .build();
    }
}