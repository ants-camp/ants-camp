package io.antcamp.assistantservice.application.dto.command;

import java.util.List;
import java.util.UUID;

public record RunPairwiseCommand(
        UUID evalRunIdA,
        UUID evalRunIdB,
        List<String> judgeModels
) {}