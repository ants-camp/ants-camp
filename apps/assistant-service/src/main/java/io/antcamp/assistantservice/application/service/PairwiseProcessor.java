package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.dto.command.RunPairwiseCommand;
import io.antcamp.assistantservice.application.port.JudgeLlmPort;
import io.antcamp.assistantservice.domain.model.PairwiseResult;
import io.antcamp.assistantservice.domain.model.RagQuerySnapshot;
import io.antcamp.assistantservice.domain.model.Verdict;
import io.antcamp.assistantservice.domain.repository.PairwiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PairwiseProcessor {

    private final JudgeLlmPort judgeLlmPort;
    private final PairwiseRepository pairwiseRepository;

    @Async("evalExecutor")
    public void comparePairwise(RunPairwiseCommand command) {
        Map<String, String> responsesA = loadResponses(command.evalRunIdA().toString());
        Map<String, String> responsesB = loadResponses(command.evalRunIdB().toString());

        // 두 Run에 공통으로 존재하는 질문만 비교
        Set<String> sharedQuestions = responsesA.keySet().stream()
                .filter(responsesB::containsKey)
                .collect(Collectors.toSet());

        log.info("Pairwise 비교 시작: runA={}, runB={}, 공통 질문 수={}",
                command.evalRunIdA(), command.evalRunIdB(), sharedQuestions.size());

        for (String question : sharedQuestions) {
            for (String judgeModel : command.judgeModels()) {
                compareAndSave(command, question,
                        responsesA.get(question), responsesB.get(question), judgeModel);
            }
        }
    }

    private Map<String, String> loadResponses(String evalRunId) {
        return pairwiseRepository
                .findRagSnapshotsByRunId(java.util.UUID.fromString(evalRunId))
                .stream()
                .collect(Collectors.toMap(RagQuerySnapshot::userQuery, RagQuerySnapshot::llmResponse));
    }

    private void compareAndSave(RunPairwiseCommand command, String question,
                                  String responseA, String responseB, String judgeModel) {
        try {
            Verdict verdict = judgeLlmPort.compare(judgeModel, question, responseA, responseB);
            pairwiseRepository.save(PairwiseResult.create(
                    command.evalRunIdA(), command.evalRunIdB(), question, judgeModel, verdict));
            log.info("Pairwise 판정 완료: judgeModel={}, question={}, verdict={}", judgeModel, question, verdict);
        } catch (Exception e) {
            log.error("Pairwise 판정 실패, 해당 조합 건너뜀: judgeModel={}, question={}", judgeModel, question, e);
        }
    }
}