package io.antcamp.assistantservice.infrastructure.persistence.jpa;

import io.antcamp.assistantservice.domain.model.PairwiseRunStatus;
import io.antcamp.assistantservice.infrastructure.entity.PairwiseRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaPairwiseRunRepository extends JpaRepository<PairwiseRunEntity, UUID> {

    Optional<PairwiseRunEntity> findTopByEvalRunIdAAndEvalRunIdBOrderByCreatedAtDesc(
            UUID evalRunIdA, UUID evalRunIdB);

    @Modifying
    @Query("UPDATE PairwiseRunEntity r SET r.status = :status WHERE r.pairwiseRunId = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") PairwiseRunStatus status);

    @Modifying
    @Query("UPDATE PairwiseRunEntity r SET r.totalCount = :total, r.status = :status WHERE r.pairwiseRunId = :id")
    void updateTotalAndStatus(@Param("id") UUID id, @Param("total") int total,
                              @Param("status") PairwiseRunStatus status);

    @Modifying
    @Query("UPDATE PairwiseRunEntity r SET r.doneCount = r.doneCount + 1 WHERE r.pairwiseRunId = :id")
    void incrementDone(@Param("id") UUID id);
}