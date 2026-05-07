package io.antcamp.assistantservice.infrastructure.persistence.adapter;

import io.antcamp.assistantservice.domain.model.PairwiseResult;
import io.antcamp.assistantservice.domain.model.PairwiseSummary;
import io.antcamp.assistantservice.domain.model.RagQuerySnapshot;
import io.antcamp.assistantservice.domain.repository.PairwiseRepository;
import io.antcamp.assistantservice.infrastructure.entity.PairwiseResultEntity;
import io.antcamp.assistantservice.infrastructure.persistence.jpa.JpaPairwiseResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PairwisePersistenceAdapter implements PairwiseRepository {

    private final JpaPairwiseResultRepository jpaRepository;

    @Transactional
    @Override
    public PairwiseResult save(PairwiseResult result) {
        return jpaRepository.save(PairwiseResultEntity.from(result)).toDomain();
    }

    @Override
    public List<RagQuerySnapshot> findRagSnapshotsByRunId(UUID evalRunId) {
        return jpaRepository.findRagSnapshotsRaw(evalRunId).stream()
                .map(row -> new RagQuerySnapshot(
                        UUID.fromString(row[0].toString()),
                        row[1].toString(),
                        row[2].toString()
                ))
                .toList();
    }

    @Override
    public PairwiseSummary findSummary(UUID evalRunIdA, UUID evalRunIdB) {
        List<PairwiseSummary.JudgeSummary> byJudge = jpaRepository
                .findSummaryRaw(evalRunIdA, evalRunIdB).stream()
                .map(row -> new PairwiseSummary.JudgeSummary(
                        row[0].toString(),
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toLong(row[4])
                ))
                .toList();
        return new PairwiseSummary(evalRunIdA, evalRunIdB, byJudge);
    }

    private long toLong(Object val) {
        return val instanceof Number n ? n.longValue() : 0L;
    }
}