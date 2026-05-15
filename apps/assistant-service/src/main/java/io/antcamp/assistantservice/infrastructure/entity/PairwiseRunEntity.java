package io.antcamp.assistantservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.PairwiseRun;
import io.antcamp.assistantservice.domain.model.PairwiseRunStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "p_pairwise_runs")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PairwiseRunEntity extends BaseEntity {

    @Id
    @Column(name = "pairwise_run_id", updatable = false, nullable = false)
    private UUID pairwiseRunId;

    @Column(name = "eval_run_id_a", nullable = false)
    private UUID evalRunIdA;

    @Column(name = "eval_run_id_b", nullable = false)
    private UUID evalRunIdB;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PairwiseRunStatus status;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "done_count", nullable = false)
    private int doneCount;

    public static PairwiseRunEntity from(PairwiseRun domain) {
        return PairwiseRunEntity.builder()
                .pairwiseRunId(domain.getPairwiseRunId())
                .evalRunIdA(domain.getEvalRunIdA())
                .evalRunIdB(domain.getEvalRunIdB())
                .status(domain.getStatus())
                .totalCount(domain.getTotalCount())
                .doneCount(domain.getDoneCount())
                .build();
    }

    public PairwiseRun toDomain() {
        return PairwiseRun.restore(
                this.pairwiseRunId, this.evalRunIdA, this.evalRunIdB,
                this.status, this.totalCount, this.doneCount);
    }
}