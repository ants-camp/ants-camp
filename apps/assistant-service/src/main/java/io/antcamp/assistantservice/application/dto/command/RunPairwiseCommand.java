package io.antcamp.assistantservice.application.dto.command;

import io.antcamp.assistantservice.domain.exception.InvalidEvaluationException;
import io.antcamp.assistantservice.domain.exception.InvalidInputException;

import java.util.List;
import java.util.UUID;

public record RunPairwiseCommand(
        UUID evalRunIdA,
        UUID evalRunIdB,
        List<String> judgeModels
) {
    public RunPairwiseCommand {
        if (evalRunIdA == null) throw new InvalidInputException();
        if (evalRunIdB == null) throw new InvalidInputException();
        if (judgeModels == null || judgeModels.isEmpty()) throw InvalidEvaluationException.judgeModelsEmpty();
    }
}