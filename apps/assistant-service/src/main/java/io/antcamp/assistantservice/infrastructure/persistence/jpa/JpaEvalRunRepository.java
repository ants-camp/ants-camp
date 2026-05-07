package io.antcamp.assistantservice.infrastructure.persistence.jpa;

import io.antcamp.assistantservice.domain.model.EvalRunStatus;
import io.antcamp.assistantservice.infrastructure.entity.EvalRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaEvalRunRepository extends JpaRepository<EvalRunEntity, UUID> {

    @Modifying
    @Query("UPDATE EvalRunEntity e SET e.status = :status WHERE e.evalRunId = :evalRunId")
    void updateStatus(@Param("evalRunId") UUID evalRunId, @Param("status") EvalRunStatus status);

    @Query("SELECT e.status FROM EvalRunEntity e WHERE e.evalRunId = :evalRunId")
    EvalRunStatus findStatusById(@Param("evalRunId") UUID evalRunId);
}