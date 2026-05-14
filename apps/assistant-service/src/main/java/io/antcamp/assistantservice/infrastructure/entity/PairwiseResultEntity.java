package io.antcamp.assistantservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.PairwiseResult;
import io.antcamp.assistantservice.domain.model.Verdict;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "p_pairwise_results")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PairwiseResultEntity extends BaseEntity {

    @Id
    @Column(name = "pairwise_result_id", updatable = false, nullable = false)
    private UUID pairwiseResultId;

    @Column(name = "eval_run_id_a", nullable = false)
    private UUID evalRunIdA;

    @Column(name = "eval_run_id_b", nullable = false)
    private UUID evalRunIdB;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "judge_model", nullable = false, length = 50)
    private String judgeModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = false, length = 10)
    private Verdict verdict;

    public static PairwiseResultEntity from(PairwiseResult domain) {
        return PairwiseResultEntity.builder()
                .pairwiseResultId(domain.getPairwiseResultId())
                .evalRunIdA(domain.getEvalRunIdA())
                .evalRunIdB(domain.getEvalRunIdB())
                .question(domain.getQuestion())
                .judgeModel(domain.getJudgeModel())
                .verdict(domain.getVerdict())
                .build();
    }

    public PairwiseResult toDomain() {
        return PairwiseResult.restore(
                this.pairwiseResultId, this.evalRunIdA, this.evalRunIdB,
                this.question, this.judgeModel, this.verdict);
    }
}