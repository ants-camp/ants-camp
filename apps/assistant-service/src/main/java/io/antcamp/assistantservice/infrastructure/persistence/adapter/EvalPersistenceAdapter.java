package io.antcamp.assistantservice.infrastructure.persistence.adapter;

import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.model.EvalResult;
import io.antcamp.assistantservice.domain.model.EvalResultView;
import io.antcamp.assistantservice.domain.model.EvalRun;
import io.antcamp.assistantservice.domain.model.EvalRunStatus;
import io.antcamp.assistantservice.domain.model.EvalSummary;
import io.antcamp.assistantservice.domain.model.RagQuery;
import io.antcamp.assistantservice.domain.repository.EvalRepository;
import io.antcamp.assistantservice.infrastructure.entity.EvalResultEntity;
import io.antcamp.assistantservice.infrastructure.entity.EvalRunEntity;
import io.antcamp.assistantservice.infrastructure.entity.RagQueryEntity;
import io.antcamp.assistantservice.infrastructure.persistence.jpa.JpaEvalResultRepository;
import io.antcamp.assistantservice.infrastructure.persistence.jpa.JpaEvalRunRepository;
import io.antcamp.assistantservice.infrastructure.persistence.jpa.JpaRagQueryRepository;
import io.antcamp.assistantservice.infrastructure.persistence.query.EvalResultQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EvalPersistenceAdapter implements EvalRepository {

    private static final Object[] EMPTY_SUMMARY = {null, null, null, null};

    private final JpaEvalRunRepository jpaEvalRunRepository;
    private final JpaRagQueryRepository jpaRagQueryRepository;
    private final JpaEvalResultRepository jpaEvalResultRepository;
    private final EvalResultQueryRepository evalResultQueryRepository;

    @Transactional
    @Override
    public EvalRun saveEvalRun(EvalRun evalRun) {
        return jpaEvalRunRepository.save(EvalRunEntity.from(evalRun)).toDomain();
    }

    @Override
    public Optional<EvalRun> findEvalRunById(UUID evalRunId) {
        return jpaEvalRunRepository.findById(evalRunId).map(EvalRunEntity::toDomain);
    }

    @Transactional
    @Override
    public void markRunning(UUID evalRunId) {
        jpaEvalRunRepository.updateStatus(evalRunId, EvalRunStatus.RUNNING);
    }

    @Transactional
    @Override
    public void markCompleted(UUID evalRunId) {
        jpaEvalRunRepository.updateStatus(evalRunId, EvalRunStatus.COMPLETED);
    }

    @Transactional
    @Override
    public void markFailed(UUID evalRunId) {
        jpaEvalRunRepository.updateStatus(evalRunId, EvalRunStatus.FAILED);
    }

    @Override
    public Optional<EvalRunStatus> findRunStatus(UUID evalRunId) {
        return Optional.ofNullable(jpaEvalRunRepository.findStatusById(evalRunId));
    }

    @Transactional
    @Override
    public RagQuery saveRagQuery(RagQuery ragQuery) {
        return jpaRagQueryRepository.save(RagQueryEntity.from(ragQuery)).toDomain();
    }

    @Transactional
    @Override
    public EvalResult saveEvalResult(EvalResult evalResult) {
        return jpaEvalResultRepository.save(EvalResultEntity.from(evalResult)).toDomain();
    }

    @Override
    public CursorSlice<EvalResultView, LocalDateTime> findResults(
            String judgeModel, LocalDateTime lastUpdatedAt, LocalDate startDate, LocalDate endDate, UUID runId, int pageSize) {
        return evalResultQueryRepository.findResults(judgeModel, lastUpdatedAt, startDate, endDate, runId, pageSize);
    }

    @Override
    public EvalSummary calculateSummary(String judgeModel, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        // 전체 평균 — Hibernate가 단일행 집계를 List<Object[]>로 감싸서 반환
        Object[] total = jpaEvalResultRepository.calculateSummaryRaw(judgeModel, startDateTime, endDateTime)
                .stream().findFirst().orElse(EMPTY_SUMMARY);

        // Judge별 평균
        List<EvalSummary.JudgeSummary> byJudge = jpaEvalResultRepository
                .calculateSummaryByJudgeRaw(judgeModel, startDateTime, endDateTime).stream()
                .map(row -> new EvalSummary.JudgeSummary(
                        row[0].toString(),
                        toDouble(row[1]),
                        toDouble(row[2]),
                        toDouble(row[3]),
                        toLong(row[4])
                ))
                .toList();

        return new EvalSummary(
                byJudge,
                toDouble(total[0]),
                toDouble(total[1]),
                toDouble(total[2]),
                toLong(total[3])
        );
    }

    private double toDouble(Object val) {
        return val instanceof Number n ? n.doubleValue() : 0.0;
    }

    private long toLong(Object val) {
        return val instanceof Number n ? n.longValue() : 0L;
    }
}