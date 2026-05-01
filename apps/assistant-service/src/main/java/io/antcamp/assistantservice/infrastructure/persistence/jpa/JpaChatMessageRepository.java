package io.antcamp.assistantservice.infrastructure.persistence.jpa;

import io.antcamp.assistantservice.domain.model.MessageStatus;
import io.antcamp.assistantservice.domain.model.Role;
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

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.role = :role AND m.status = :status AND m.createdAt < :threshold")
    List<ChatMessageEntity> findPendingUserMessages(
            @Param("role") Role role,
            @Param("status") MessageStatus status,
            @Param("threshold") LocalDateTime threshold
    );
}