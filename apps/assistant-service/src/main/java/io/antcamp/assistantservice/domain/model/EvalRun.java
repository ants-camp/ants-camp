package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EvalRun {

    private UUID evalRunId;
    private List<String> questions;
    private List<String> judgeModels;
    private UUID promptVersionId;
    private String memo;
    private EvalRunStatus status;

    public static EvalRun create(List<String> questions, List<String> judgeModels,
                                  UUID promptVersionId, String memo) {
        return EvalRun.builder()
                .evalRunId(UUID.randomUUID())
                .questions(List.copyOf(questions))
                .judgeModels(List.copyOf(judgeModels))
                .promptVersionId(promptVersionId)
                .memo(memo)
                .status(EvalRunStatus.PENDING)
                .build();
    }

    public static EvalRun restore(UUID evalRunId, List<String> questions, List<String> judgeModels,
                                   UUID promptVersionId, String memo, EvalRunStatus status) {
        return EvalRun.builder()
                .evalRunId(evalRunId)
                .questions(questions)
                .judgeModels(judgeModels)
                .promptVersionId(promptVersionId)
                .memo(memo)
                .status(status)
                .build();
    }

    public EvalRun start() {
        return restore(evalRunId, questions, judgeModels, promptVersionId, memo, EvalRunStatus.RUNNING);
    }

    public EvalRun complete() {
        return restore(evalRunId, questions, judgeModels, promptVersionId, memo, EvalRunStatus.COMPLETED);
    }

    public EvalRun fail() {
        return restore(evalRunId, questions, judgeModels, promptVersionId, memo, EvalRunStatus.FAILED);
    }
}