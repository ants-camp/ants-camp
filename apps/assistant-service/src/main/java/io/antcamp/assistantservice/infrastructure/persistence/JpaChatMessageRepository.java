package io.antcamp.assistantservice.infrastructure.persistence;

import io.antcamp.assistantservice.infrastructure.entity.ChatMessageEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    List<ChatMessageEntity> findByChatSessionIdOrderBySeqAsc(UUID chatSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COALESCE(MAX(m.seq), 0) FROM ChatMessageEntity m WHERE m.chatSessionId = :chatSessionId")
    int findMaxSeqForUpdate(@Param("chatSessionId") UUID chatSessionId);

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.role = 'USER' AND m.status = 'PENDING' AND m.createdAt < :threshold")
    List<ChatMessageEntity> findPendingUserMessages(@Param("threshold") LocalDateTime threshold);
}