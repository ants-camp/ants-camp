package io.antcamp.assistantservice.infrastructure.persistence.jpa;

import io.antcamp.assistantservice.infrastructure.entity.PromptVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPromptVersionRepository extends JpaRepository<PromptVersionEntity, UUID> {
}