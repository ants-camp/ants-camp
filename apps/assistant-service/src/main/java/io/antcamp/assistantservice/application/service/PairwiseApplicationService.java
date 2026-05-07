package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.dto.command.RunPairwiseCommand;
import io.antcamp.assistantservice.domain.model.PairwiseSummary;
import io.antcamp.assistantservice.domain.repository.PairwiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PairwiseApplicationService {

    private final PairwiseProcessor pairwiseProcessor;
    private final PairwiseRepository pairwiseRepository;

    public void runPairwise(RunPairwiseCommand command) {
        pairwiseProcessor.comparePairwise(command);
    }

    public PairwiseSummary getSummary(UUID evalRunIdA, UUID evalRunIdB) {
        return pairwiseRepository.findSummary(evalRunIdA, evalRunIdB);
    }
}