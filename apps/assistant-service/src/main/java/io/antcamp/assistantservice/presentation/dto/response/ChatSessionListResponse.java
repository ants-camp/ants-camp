package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.application.dto.result.ChatSessionResult;
import io.antcamp.assistantservice.domain.model.CursorSlice;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSessionListResponse(List<ChatSessionResponse> items, boolean hasNext, LocalDateTime nextCursor) {

    public static ChatSessionListResponse from(CursorSlice<ChatSessionResult, LocalDateTime> slice) {
        return new ChatSessionListResponse(
                slice.items().stream().map(ChatSessionResponse::from).toList(),
                slice.hasNext(),
                slice.nextCursor()
        );
    }
}