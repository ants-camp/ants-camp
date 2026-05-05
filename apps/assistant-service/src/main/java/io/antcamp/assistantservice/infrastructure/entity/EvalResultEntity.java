package io.antcamp.assistantservice.infrastructure.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.EvalResult;
import io.antcamp.assistantservice.domain.model.EvalScores;
import io.antcamp.assistantservice.infrastructure.util.JsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "p_eval_results")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvalResultEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "eval_result_id", updatable = false, nullable = false)
    private UUID evalResultId;

    @Column(name = "eval_run_id", nullable = false)
    private UUID evalRunId;

    @Column(name = "rag_query_id", nullable = false)
    private UUID ragQueryId;

    @Column(name = "judge_model", nullable = false, length = 50)
    private String judgeModel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scores", nullable = false, columnDefinition = "jsonb")
    private String scores;

    @Column(name = "judge_latency_ms")
    private Integer judgeLatencyMs;

    @Column(name = "judge_prompt_tokens")
    private Integer judgePromptTokens;

    @Column(name = "judge_completion_tokens")
    private Integer judgeCompletionTokens;

    public static EvalResultEntity from(EvalResult domain) {
        return EvalResultEntity.builder()
                .evalRunId(domain.getEvalRunId())
                .ragQueryId(domain.getRagQueryId())
                .judgeModel(domain.getJudgeModel())
                .scores(JsonConverter.toJson(domain.getScores()))
                .judgeLatencyMs(domain.getJudgeLatencyMs())
                .judgePromptTokens(domain.getJudgePromptTokens())
                .judgeCompletionTokens(domain.getJudgeCompletionTokens())
                .build();
    }

    public EvalResult toDomain() {
        EvalScores evalScores = JsonConverter.fromJson(this.scores, new TypeReference<>() {});
        return EvalResult.restore(this.evalResultId, this.evalRunId, this.ragQueryId, this.judgeModel,
                evalScores, this.judgeLatencyMs, this.judgePromptTokens, this.judgeCompletionTokens);
    }
}