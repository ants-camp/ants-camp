package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.dto.command.RunEvaluationCommand;
import io.antcamp.assistantservice.application.dto.query.GetEvalResultsQuery;
import io.antcamp.assistantservice.application.dto.result.EvalResultItemResult;
import io.antcamp.assistantservice.application.dto.result.EvalResultListResult;
import io.antcamp.assistantservice.application.dto.result.EvalSummaryResult;
import io.antcamp.assistantservice.domain.exception.EvalRunNotFoundException;
import io.antcamp.assistantservice.domain.model.*;
import io.antcamp.assistantservice.infrastructure.config.LlmConfig;
import io.antcamp.assistantservice.domain.repository.EvalRepository;
import io.antcamp.assistantservice.domain.repository.PromptVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvalApplicationService {

    private static final int PAGE_SIZE = 30;

    private final EvalProcessor evalProcessor;
    private final EvalRepository evalRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final LlmConfig llmConfig;

    public UUID runEvaluation(RunEvaluationCommand command) {
        // 지정 프롬프트 버전의 내용을 미리 조회 (없으면 기본 프롬프트 사용)
        String promptOverride = Optional.ofNullable(command.promptVersionId())
                .flatMap(promptVersionRepository::findById)
                .map(PromptVersion::getContent)
                .orElse(null);

        // ragModel 미지정 시 기본 설정 모델 사용
        String resolvedRagModel = command.ragModel() != null ? command.ragModel() : llmConfig.modelName();

        List<String> questionTexts = command.questions().stream()
                .map(EvalQuestion::question)
                .toList();
        EvalRun evalRun = evalRepository.saveEvalRun(
                EvalRun.create(questionTexts, command.judgeModels(),
                        command.promptVersionId(), resolvedRagModel, command.memo()));
        evalProcessor.runEvalPipeline(command, evalRun.getEvalRunId(), promptOverride, resolvedRagModel);
        return evalRun.getEvalRunId();
    }

    public EvalRunStatus getRunStatus(UUID evalRunId) {
        return evalRepository.findRunStatus(evalRunId).orElseThrow(EvalRunNotFoundException::new);
    }

    public EvalResultListResult getEvalResults(GetEvalResultsQuery query) {
        CursorSlice<EvalResultView, LocalDateTime> slice = evalRepository.findResults(
                query.judgeModel(), query.lastUpdatedAt(), query.startDate(), query.endDate(), PAGE_SIZE);

        List<EvalResultItemResult> content = slice.items().stream()
                .map(EvalResultItemResult::from)
                .toList();

        EvalSummary summary = evalRepository.calculateSummary(
                query.judgeModel(), query.startDate(), query.endDate());

        return new EvalResultListResult(
                content,
                EvalSummaryResult.from(summary),
                slice.hasNext(),
                slice.nextCursor()
        );
    }
}