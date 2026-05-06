package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.EvalResultListResult;

import java.time.LocalDateTime;
import java.util.List;

public record EvalResultListResponse(
        List<EvalItemResponse> content,
        EvalSummaryResponse summary,
        boolean hasNext,
        LocalDateTime lastUpdatedAt
) {
    public static EvalResultListResponse from(EvalResultListResult result) {
        return new EvalResultListResponse(
                result.content().stream().map(EvalItemResponse::from).toList(),
                EvalSummaryResponse.from(result.summary()),
                result.hasNext(),
                result.lastUpdatedAt()
        );
    }
}