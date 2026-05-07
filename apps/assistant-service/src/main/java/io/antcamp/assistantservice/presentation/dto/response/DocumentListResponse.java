package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.DocumentListResult;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentListResponse(
        List<DocumentItemResponse> items,
        boolean hasNext,
        LocalDateTime nextCursor
) {

    public static DocumentListResponse from(DocumentListResult result) {
        return new DocumentListResponse(
                result.items().stream().map(DocumentItemResponse::from).toList(),
                result.hasNext(),
                result.nextCursor()
        );
    }
}
