package io.antcamp.assistantservice.infrastructure.persistence;

import io.antcamp.assistantservice.infrastructure.entity.RagQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaRagQueryRepository extends JpaRepository<RagQueryEntity, UUID> {
}