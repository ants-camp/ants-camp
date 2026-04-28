package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EvalResult {

    private UUID evalResultId;
    private UUID ragQueryId;
    private String judgeModel;
    private EvalScores scores;

    public static EvalResult create(UUID ragQueryId, String judgeModel, EvalScores scores) {
        return EvalResult.builder()
                .evalResultId(UUID.randomUUID())
                .ragQueryId(ragQueryId)
                .judgeModel(judgeModel)
                .scores(scores)
                .build();
    }

    public static EvalResult restore(UUID evalResultId, UUID ragQueryId, String judgeModel, EvalScores scores) {
        return EvalResult.builder()
                .evalResultId(evalResultId)
                .ragQueryId(ragQueryId)
                .judgeModel(judgeModel)
                .scores(scores)
                .build();
    }
}