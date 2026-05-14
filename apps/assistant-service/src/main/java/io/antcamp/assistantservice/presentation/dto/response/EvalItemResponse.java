package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.EvalResultItemResult;
import io.antcamp.assistantservice.domain.model.RetrievedChunk;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EvalItemResponse(
        UUID evalResultId,
        UUID ragQueryId,
        String question,
        String llmResponse,
        String judgeModel,
        double relevance,
        double faithfulness,
        double contextPrecision,
        String feedback,
        LocalDateTime evaluatedAt,
        List<RetrievedChunk> retrievedChunks,
        String promptUsed
) {
    public static EvalItemResponse from(EvalResultItemResult result) {
        return new EvalItemResponse(
                result.evalResultId(),
                result.ragQueryId(),
                result.question(),
                result.llmResponse(),
                result.judgeModel(),
                result.relevance(),
                result.faithfulness(),
                result.contextPrecision(),
                result.feedback(),
                result.evaluatedAt(),
                result.retrievedChunks(),
                result.promptUsed()
        );
    }
}