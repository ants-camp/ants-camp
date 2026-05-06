package io.antcamp.assistantservice.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentListResult(List<DocumentItemResult> items, boolean hasNext, LocalDateTime nextCursor) {
}