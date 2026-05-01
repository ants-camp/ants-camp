package io.antcamp.assistantservice.infrastructure.persistence.jpa;

import io.antcamp.assistantservice.infrastructure.entity.PairwiseResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaPairwiseResultRepository extends JpaRepository<PairwiseResultEntity, UUID> {

    // Run 쌍에 대한 Judge별 판정 집계
    @Query(value = """
            SELECT judge_model,
                   COUNT(CASE WHEN verdict = 'A_WINS' THEN 1 END) AS a_wins,
                   COUNT(CASE WHEN verdict = 'B_WINS' THEN 1 END) AS b_wins,
                   COUNT(CASE WHEN verdict = 'TIE'    THEN 1 END) AS ties,
                   COUNT(*)                                       AS total
            FROM p_pairwise_results
            WHERE eval_run_id_a = :evalRunIdA
              AND eval_run_id_b = :evalRunIdB
            GROUP BY judge_model
            """, nativeQuery = true)
    List<Object[]> findSummaryRaw(@Param("evalRunIdA") UUID evalRunIdA,
                                  @Param("evalRunIdB") UUID evalRunIdB);

    // evalRunId에 속한 고유 RagQuery 조회 (pairwise 매칭용)
    @Query(value = """
            SELECT DISTINCT ON (rq.user_query)
                rq.rag_query_id, rq.user_query, rq.llm_response
            FROM p_eval_results er
            JOIN p_rag_queries rq ON er.rag_query_id = rq.rag_query_id
            WHERE er.eval_run_id = :evalRunId
            """, nativeQuery = true)
    List<Object[]> findRagSnapshotsRaw(@Param("evalRunId") UUID evalRunId);
}