package io.antcamp.assistantservice.domain.repository;

import io.antcamp.assistantservice.domain.model.PairwiseResult;
import io.antcamp.assistantservice.domain.model.PairwiseSummary;
import io.antcamp.assistantservice.domain.model.RagQuerySnapshot;

import java.util.List;
import java.util.UUID;

public interface PairwiseRepository {

    PairwiseResult save(PairwiseResult result);

    // evalRunId에 속한 RagQuery 목록 조회 (pairwise 매칭용)
    List<RagQuerySnapshot> findRagSnapshotsByRunId(UUID evalRunId);

    // 두 Run 간 pairwise 결과 집계
    PairwiseSummary findSummary(UUID evalRunIdA, UUID evalRunIdB);
}