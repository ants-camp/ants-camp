package io.antcamp.assistantservice.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// EvalResult + RagQuery JOIN 읽기 모델
public record EvalResultView(
        UUID evalResultId,
        UUID ragQueryId,
        String question,
        String llmResponse,
        String judgeModel,
        EvalScores scores,
        LocalDateTime evaluatedAt,
        List<RetrievedChunk> retrievedChunks,  // 드릴다운: 어떤 청크가 검색됐는가
        String promptUsed                       // 드릴다운: 어떤 프롬프트가 사용됐는가
) {}