package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.dto.result.ChatMessageResult;
import io.antcamp.assistantservice.application.dto.result.ChatSessionResult;
import io.antcamp.assistantservice.domain.exception.SessionNotFoundException;
import io.antcamp.assistantservice.domain.model.ChatSession;
import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatApplicationService {

    private static final int PAGE_SIZE = 30;

    private final ChatSessionRepository chatSessionRepository;

    @Transactional
    public ChatSessionResult createSession(UUID userId) {
        ChatSession session = ChatSession.create(userId);
        ChatSession saved = chatSessionRepository.save(session);
        log.info("세션 생성: sessionId={}, userId={}", saved.getChatSessionId(), userId);
        return ChatSessionResult.from(saved);
    }

    @Transactional(readOnly = true)
    public CursorSlice<ChatSessionResult, LocalDateTime> getSessions(
            UUID userId, String keyword, LocalDateTime lastUpdatedAt) {
        CursorSlice<ChatSession, LocalDateTime> slice =
                chatSessionRepository.findSessions(userId, keyword, lastUpdatedAt, PAGE_SIZE);
        return new CursorSlice<>(
                slice.items().stream().map(ChatSessionResult::from).toList(),
                slice.hasNext(),
                slice.nextCursor()
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResult> getMessages(UUID chatSessionId, UUID userId) {
        ChatSession session = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(SessionNotFoundException::new);
        if (!session.getUserId().equals(userId)) {
            throw new SessionNotFoundException();
        }
        return chatSessionRepository.findMessages(chatSessionId).stream()
                .map(ChatMessageResult::from)
                .toList();
    }
}