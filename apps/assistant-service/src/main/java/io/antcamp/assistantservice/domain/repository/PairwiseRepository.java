package io.antcamp.assistantservice.domain.repository;

import io.antcamp.assistantservice.domain.model.PairwiseResult;
import io.antcamp.assistantservice.domain.model.PairwiseRun;
import io.antcamp.assistantservice.domain.model.PairwiseSummary;
import io.antcamp.assistantservice.domain.model.RagQuerySnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PairwiseRepository {

    PairwiseResult save(PairwiseResult result);

    // evalRunId에 속한 RagQuery 목록 조회 (pairwise 매칭용)
    List<RagQuerySnapshot> findRagSnapshotsByRunId(UUID evalRunId);

    // 두 Run 간 pairwise 결과 집계
    PairwiseSummary findSummary(UUID evalRunIdA, UUID evalRunIdB);

    // PairwiseRun 상태 관리
    PairwiseRun savePairwiseRun(PairwiseRun run);

    Optional<PairwiseRun> findLatestRun(UUID evalRunIdA, UUID evalRunIdB);

    void markRunning(UUID pairwiseRunId, int totalCount);

    void markCompleted(UUID pairwiseRunId);

    void markFailed(UUID pairwiseRunId);

    void incrementDone(UUID pairwiseRunId);
}