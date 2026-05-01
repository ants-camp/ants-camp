package io.antcamp.assistantservice.application.dto.command;

import io.antcamp.assistantservice.domain.exception.InvalidEvaluationException;
import io.antcamp.assistantservice.domain.model.EvalQuestion;

import java.util.List;
import java.util.UUID;

public record RunEvaluationCommand(
        List<EvalQuestion> questions,
        List<String> judgeModels,
        UUID promptVersionId,
        String memo
) {
    private static final int MAX_COMBINATIONS = 100; // 질문 × judge 최대 조합 수

    public RunEvaluationCommand {
        if (questions == null || questions.isEmpty()) throw InvalidEvaluationException.questionsEmpty();
        if (judgeModels == null || judgeModels.isEmpty()) throw InvalidEvaluationException.judgeModelsEmpty();
        if ((long) questions.size() * judgeModels.size() > MAX_COMBINATIONS)
            throw InvalidEvaluationException.tooManyCombinations();
    }
}