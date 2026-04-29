package io.antcamp.assistantservice.infrastructure.persistence;

import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.ChatSession;
import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.repository.ChatSessionRepository;
import io.antcamp.assistantservice.infrastructure.entity.ChatMessageEntity;
import io.antcamp.assistantservice.infrastructure.entity.ChatSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final JpaChatSessionRepository sessionJpaRepository;
    private final ChatSessionQueryRepository sessionQueryRepository;
    private final JpaChatMessageRepository messageRepository;

    @Override
    public ChatSession save(ChatSession session) {
        return sessionJpaRepository.save(ChatSessionEntity.from(session)).toDomain();
    }

    @Override
    public Optional<ChatSession> findById(UUID chatSessionId) {
        return sessionJpaRepository.findById(chatSessionId).map(ChatSessionEntity::toDomain);
    }

    @Override
    public CursorSlice<ChatSession, LocalDateTime> findSessions(
            UUID userId, String keyword, LocalDateTime lastUpdatedAt, int pageSize) {
        CursorSlice<ChatSessionEntity, LocalDateTime> slice =
                sessionQueryRepository.findSessions(userId, keyword, lastUpdatedAt, pageSize);
        return new CursorSlice<>(
                slice.items().stream().map(ChatSessionEntity::toDomain).toList(),
                slice.hasNext(),
                slice.nextCursor()
        );
    }

    @Override
    public List<ChatMessage> findMessages(UUID chatSessionId) {
        return messageRepository.findByChatSessionIdOrderBySeqAsc(chatSessionId)
                .stream()
                .map(ChatMessageEntity::toDomain)
                .toList();
    }

    @Override
    public List<ChatMessage> findPendingUserMessages(LocalDateTime createdBefore) {
        return messageRepository.findPendingUserMessages(createdBefore)
                .stream()
                .map(ChatMessageEntity::toDomain)
                .toList();
    }
}