package io.antcamp.assistantservice.infrastructure.persistence.jpa;

import io.antcamp.assistantservice.infrastructure.entity.EvalResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaEvalResultRepository extends JpaRepository<EvalResultEntity, UUID> {

    // 전체 평균 집계
    @Query(value = """
            SELECT
                AVG((scores->>'relevance')::double precision),
                AVG((scores->>'faithfulness')::double precision),
                AVG((scores->>'contextPrecision')::double precision),
                COUNT(CASE WHEN (scores->>'faithfulness')::double precision < 3.0 THEN 1 END)
            FROM p_eval_results
            WHERE deleted_at IS NULL
              AND (CAST(:judgeModel AS TEXT)      IS NULL OR judge_model = :judgeModel)
              AND (CAST(:startDate  AS TIMESTAMP) IS NULL OR created_at >= :startDate)
              AND (CAST(:endDate    AS TIMESTAMP) IS NULL OR created_at <  :endDate)
            """, nativeQuery = true)
    List<Object[]> calculateSummaryRaw(@Param("judgeModel") String judgeModel,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Judge 모델별 평균 집계
    @Query(value = """
            SELECT
                judge_model,
                AVG((scores->>'relevance')::double precision),
                AVG((scores->>'faithfulness')::double precision),
                AVG((scores->>'contextPrecision')::double precision),
                COUNT(*)
            FROM p_eval_results
            WHERE deleted_at IS NULL
              AND (CAST(:judgeModel AS TEXT)      IS NULL OR judge_model = :judgeModel)
              AND (CAST(:startDate  AS TIMESTAMP) IS NULL OR created_at >= :startDate)
              AND (CAST(:endDate    AS TIMESTAMP) IS NULL OR created_at <  :endDate)
            GROUP BY judge_model
            ORDER BY judge_model
            """, nativeQuery = true)
    List<Object[]> calculateSummaryByJudgeRaw(@Param("judgeModel") String judgeModel,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}