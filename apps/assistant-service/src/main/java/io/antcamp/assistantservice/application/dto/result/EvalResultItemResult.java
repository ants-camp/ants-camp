package io.antcamp.assistantservice.application.dto.result;

import io.antcamp.assistantservice.domain.model.EvalResultView;
import io.antcamp.assistantservice.domain.model.RetrievedChunk;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EvalResultItemResult(
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
    public static EvalResultItemResult from(EvalResultView view) {
        return new EvalResultItemResult(
                view.evalResultId(),
                view.ragQueryId(),
                view.question(),
                view.llmResponse(),
                view.judgeModel(),
                view.scores().relevance(),
                view.scores().faithfulness(),
                view.scores().contextPrecision(),
                view.scores().feedback(),
                view.evaluatedAt(),
                view.retrievedChunks(),
                view.promptUsed()
        );
    }
}