package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class PairwiseResult {

    private UUID pairwiseResultId;
    private UUID evalRunIdA;
    private UUID evalRunIdB;
    private String question;
    private String judgeModel;
    private Verdict verdict;

    public static PairwiseResult create(UUID evalRunIdA, UUID evalRunIdB,
                                         String question, String judgeModel, Verdict verdict) {
        return PairwiseResult.builder()
                .pairwiseResultId(UUID.randomUUID())
                .evalRunIdA(evalRunIdA)
                .evalRunIdB(evalRunIdB)
                .question(question)
                .judgeModel(judgeModel)
                .verdict(verdict)
                .build();
    }

    public static PairwiseResult restore(UUID pairwiseResultId, UUID evalRunIdA, UUID evalRunIdB,
                                          String question, String judgeModel, Verdict verdict) {
        return PairwiseResult.builder()
                .pairwiseResultId(pairwiseResultId)
                .evalRunIdA(evalRunIdA)
                .evalRunIdB(evalRunIdB)
                .question(question)
                .judgeModel(judgeModel)
                .verdict(verdict)
                .build();
    }
}