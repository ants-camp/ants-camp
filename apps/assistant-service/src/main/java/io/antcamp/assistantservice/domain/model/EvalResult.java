package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EvalResult {

    private UUID evalResultId;
    private UUID evalRunId;
    private UUID ragQueryId;
    private String judgeModel;
    private EvalScores scores;
    private Integer judgeLatencyMs;
    private Integer judgePromptTokens;
    private Integer judgeCompletionTokens;

    public static EvalResult create(UUID evalRunId, UUID ragQueryId, String judgeModel, EvalScores scores,
                                     int judgeLatencyMs, int judgePromptTokens, int judgeCompletionTokens) {
        return EvalResult.builder()
                .evalResultId(UUID.randomUUID())
                .evalRunId(evalRunId)
                .ragQueryId(ragQueryId)
                .judgeModel(judgeModel)
                .scores(scores)
                .judgeLatencyMs(judgeLatencyMs)
                .judgePromptTokens(judgePromptTokens)
                .judgeCompletionTokens(judgeCompletionTokens)
                .build();
    }

    public static EvalResult restore(UUID evalResultId, UUID evalRunId, UUID ragQueryId, String judgeModel,
                                      EvalScores scores, Integer judgeLatencyMs,
                                      Integer judgePromptTokens, Integer judgeCompletionTokens) {
        return EvalResult.builder()
                .evalResultId(evalResultId)
                .evalRunId(evalRunId)
                .ragQueryId(ragQueryId)
                .judgeModel(judgeModel)
                .scores(scores)
                .judgeLatencyMs(judgeLatencyMs)
                .judgePromptTokens(judgePromptTokens)
                .judgeCompletionTokens(judgeCompletionTokens)
                .build();
    }
}