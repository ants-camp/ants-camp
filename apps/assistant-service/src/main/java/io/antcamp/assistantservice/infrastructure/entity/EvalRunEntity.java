package io.antcamp.assistantservice.infrastructure.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.EvalRun;
import io.antcamp.assistantservice.domain.model.EvalRunStatus;
import io.antcamp.assistantservice.infrastructure.util.JsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_eval_runs")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvalRunEntity extends BaseEntity {

    @Id
    @Column(name = "eval_run_id", updatable = false, nullable = false)
    private UUID evalRunId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", nullable = false, columnDefinition = "jsonb")
    private String questions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "judge_models", nullable = false, columnDefinition = "jsonb")
    private String judgeModels;

    @Column(name = "prompt_version_id")
    private UUID promptVersionId;

    @Column(name = "rag_model", nullable = false, length = 50)
    private String ragModel;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EvalRunStatus status;

    public static EvalRunEntity from(EvalRun domain) {
        return EvalRunEntity.builder()
                .evalRunId(domain.getEvalRunId())
                .questions(JsonConverter.toJson(domain.getQuestions()))
                .judgeModels(JsonConverter.toJson(domain.getJudgeModels()))
                .promptVersionId(domain.getPromptVersionId())
                .ragModel(domain.getRagModel())
                .memo(domain.getMemo())
                .status(domain.getStatus())
                .build();
    }

    public EvalRun toDomain() {
        List<String> questionTexts = JsonConverter.fromJson(this.questions, new TypeReference<>() {});
        List<String> judgeModels = JsonConverter.fromJson(this.judgeModels, new TypeReference<>() {});
        return EvalRun.restore(this.evalRunId, questionTexts, judgeModels, this.promptVersionId, this.ragModel, this.memo, this.status);
    }
}