package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.dto.command.RunEvaluationCommand;
import io.antcamp.assistantservice.application.port.JudgeLlmPort;
import io.antcamp.assistantservice.domain.model.EvalQuestion;
import io.antcamp.assistantservice.domain.model.EvalResult;
import io.antcamp.assistantservice.domain.model.RagQuery;
import io.antcamp.assistantservice.domain.repository.EvalRepository;
import io.antcamp.assistantservice.infrastructure.config.LlmConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvalProcessor {

    private final RagApplicationService ragApplicationService;
    private final JudgeLlmPort judgeLlmPort;
    private final EvalRepository evalRepository;
    private final LlmConfig llmConfig;

    @Async("evalExecutor")
    public void runEvalPipeline(RunEvaluationCommand command, UUID evalRunId, String promptOverride) {
        evalRepository.markRunning(evalRunId);
        try {
            for (EvalQuestion evalQuestion : command.questions()) {
                RagApplicationService.EvalRagResult ragResult = runRag(evalQuestion.question(), promptOverride);
                if (ragResult == null) continue;

                RagQuery ragQuery = saveRagQuery(evalQuestion.question(), ragResult);
                if (ragQuery == null) continue;

                for (String judgeModel : command.judgeModels()) {
                    judgeAndSave(ragQuery, ragResult.contextText(), judgeModel,
                            evalQuestion.question(), evalQuestion.referenceAnswer(), evalRunId);
                }
            }
            evalRepository.markCompleted(evalRunId);
            log.info("평가 파이프라인 완료: evalRunId={}", evalRunId);
        } catch (Exception e) {
            evalRepository.markFailed(evalRunId);
            log.error("평가 파이프라인 실패: evalRunId={}", evalRunId, e);
        }
    }

    private RagApplicationService.EvalRagResult runRag(String question, String promptOverride) {
        try {
            return ragApplicationService.runRagForEval(question, promptOverride);
        } catch (Exception e) {
            log.error("평가용 RAG 실행 실패, 해당 질문 건너뜀: question={}", question, e);
            return null;
        }
    }

    private RagQuery saveRagQuery(String question, RagApplicationService.EvalRagResult ragResult) {
        try {
            return evalRepository.saveRagQuery(RagQuery.createForEval(
                    ragResult.userQuery(),
                    ragResult.retrievedChunks(),
                    ragResult.promptUsed(),
                    ragResult.llmModel(),
                    ragResult.llmResponse(),
                    ragResult.latencyMs(),
                    ragResult.promptTokens(),
                    ragResult.completionTokens(),
                    ragResult.topK()
            ));
        } catch (Exception e) {
            log.error("RAG 쿼리 저장 실패, 해당 질문 건너뜀: question={}", question, e);
            return null;
        }
    }

    private void judgeAndSave(RagQuery ragQuery, String contextText, String judgeModel,
                              String question, String referenceAnswer, UUID evalRunId) {
        // 응답 생성 모델과 Judge 모델이 동일하면 self-preference bias 발생 → skip
        if (judgeModel.equalsIgnoreCase(llmConfig.modelName())) {
            log.warn("Self-preference bias 방지: RAG 생성 모델과 Judge 모델이 동일하여 skip. model={}", judgeModel);
            return;
        }
        try {
            JudgeLlmPort.JudgeEvalResult judgeResult = judgeLlmPort.evaluate(
                    judgeModel, question, ragQuery.getLlmResponse(), contextText, referenceAnswer);
            evalRepository.saveEvalResult(EvalResult.create(evalRunId, ragQuery.getRagQueryId(), judgeModel,
                    judgeResult.scores(), judgeResult.latencyMs(),
                    judgeResult.promptTokens(), judgeResult.completionTokens()));
            log.info("평가 완료: evalRunId={}, question={}, judgeModel={}, relevance={}, faithfulness={}, latencyMs={}",
                    evalRunId, question, judgeModel, judgeResult.scores().relevance(),
                    judgeResult.scores().faithfulness(), judgeResult.latencyMs());
        } catch (Exception e) {
            log.error("Judge 채점 실패, 해당 조합 건너뜀: judgeModel={}, question={}", judgeModel, question, e);
        }
    }
}