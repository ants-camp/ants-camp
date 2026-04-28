package io.antcamp.assistantservice.infrastructure.persistence;

import io.antcamp.assistantservice.domain.model.IngestStatus;
import io.antcamp.assistantservice.infrastructure.entity.KnowledgeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaKnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, UUID> {

    @Query("SELECT d.documentId FROM KnowledgeDocumentEntity d WHERE d.ingestStatus = :status")
    List<UUID> findIdsByIngestStatus(@Param("status") IngestStatus status);

}