package io.antcamp.assistantservice.domain.repository;

import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.ChatSession;
import io.antcamp.assistantservice.domain.model.CursorSlice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository {

    ChatSession save(ChatSession chatSession);

    Optional<ChatSession> findById(UUID chatSessionId);

    CursorSlice<ChatSession, LocalDateTime> findSessions(UUID userId, String keyword, LocalDateTime lastUpdatedAt, int pageSize);

    List<ChatMessage> findMessages(UUID chatSessionId);

    List<ChatMessage> findPendingUserMessages(LocalDateTime createdBefore);
}