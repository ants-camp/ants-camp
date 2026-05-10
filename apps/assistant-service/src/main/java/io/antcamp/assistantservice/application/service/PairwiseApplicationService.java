package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.dto.command.RunPairwiseCommand;
import io.antcamp.assistantservice.domain.exception.EvalRunNotFoundException;
import io.antcamp.assistantservice.domain.exception.InvalidEvaluationException;
import io.antcamp.assistantservice.domain.model.EvalRun;
import io.antcamp.assistantservice.domain.model.PairwiseSummary;
import io.antcamp.assistantservice.domain.repository.EvalRepository;
import io.antcamp.assistantservice.domain.repository.PairwiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PairwiseApplicationService {

    private final PairwiseProcessor pairwiseProcessor;
    private final PairwiseRepository pairwiseRepository;
    private final EvalRepository evalRepository;

    public void runPairwise(RunPairwiseCommand command) {
        EvalRun runA = evalRepository.findEvalRunById(command.evalRunIdA())
                .orElseThrow(EvalRunNotFoundException::new);
        EvalRun runB = evalRepository.findEvalRunById(command.evalRunIdB())
                .orElseThrow(EvalRunNotFoundException::new);

        // 모델과 프롬프트 버전이 모두 동일하면 비교 의미 없음
        if (runA.getRagModel().equalsIgnoreCase(runB.getRagModel()) &&
                Objects.equals(runA.getPromptVersionId(), runB.getPromptVersionId())) {
            throw InvalidEvaluationException.sameRunConfig();
        }

        pairwiseProcessor.comparePairwise(command);
    }

    public PairwiseSummary getSummary(UUID evalRunIdA, UUID evalRunIdB) {
        return pairwiseRepository.findSummary(evalRunIdA, evalRunIdB);
    }

    public String getRagModel(UUID evalRunId) {
        return evalRepository.findEvalRunById(evalRunId)
                .orElseThrow(EvalRunNotFoundException::new)
                .getRagModel();
    }
}