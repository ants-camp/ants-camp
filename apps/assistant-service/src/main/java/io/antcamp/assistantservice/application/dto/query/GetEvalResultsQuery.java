package io.antcamp.assistantservice.application.dto.query;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GetEvalResultsQuery(
        String judgeModel,
        LocalDateTime lastUpdatedAt,
        LocalDate startDate,
        LocalDate endDate
) {}