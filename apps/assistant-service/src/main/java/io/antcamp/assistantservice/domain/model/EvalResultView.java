package io.antcamp.assistantservice.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// EvalResult + RagQuery + EvalRun JOIN 읽기 모델
public record EvalResultView(
        UUID evalResultId,
        UUID ragQueryId,
        String question,
        String llmResponse,
        String judgeModel,
        EvalScores scores,
        LocalDateTime evaluatedAt,
        List<RetrievedChunk> retrievedChunks,
        String promptUsed,
        UUID runId,
        String ragModel,
        List<String> judgeModels,
        String memo
) {}