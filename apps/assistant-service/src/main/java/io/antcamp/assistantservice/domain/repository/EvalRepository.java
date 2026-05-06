package io.antcamp.assistantservice.domain.repository;

import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.model.EvalResult;
import io.antcamp.assistantservice.domain.model.EvalResultView;
import io.antcamp.assistantservice.domain.model.EvalRun;
import io.antcamp.assistantservice.domain.model.EvalRunStatus;
import io.antcamp.assistantservice.domain.model.EvalSummary;
import io.antcamp.assistantservice.domain.model.RagQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EvalRepository {

    EvalRun saveEvalRun(EvalRun evalRun);

    void markRunning(UUID evalRunId);

    void markCompleted(UUID evalRunId);

    void markFailed(UUID evalRunId);

    Optional<EvalRunStatus> findRunStatus(UUID evalRunId);

    RagQuery saveRagQuery(RagQuery ragQuery);

    EvalResult saveEvalResult(EvalResult evalResult);

    CursorSlice<EvalResultView, LocalDateTime> findResults(
            String judgeModel, LocalDateTime lastUpdatedAt, LocalDate startDate, LocalDate endDate, int pageSize);

    EvalSummary calculateSummary(String judgeModel, LocalDate startDate, LocalDate endDate);
}