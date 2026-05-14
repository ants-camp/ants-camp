package io.antcamp.assistantservice.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record EvalResultListResult(
        List<EvalResultItemResult> content,
        EvalSummaryResult summary,
        boolean hasNext,
        LocalDateTime lastUpdatedAt
) {}