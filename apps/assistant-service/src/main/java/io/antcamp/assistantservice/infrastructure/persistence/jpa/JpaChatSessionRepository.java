package io.antcamp.assistantservice.infrastructure.persistence.jpa;

import io.antcamp.assistantservice.infrastructure.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaChatSessionRepository extends JpaRepository<ChatSessionEntity, UUID> {
}