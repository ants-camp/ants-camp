package io.antcamp.assistantservice.infrastructure.persistence;

import io.antcamp.assistantservice.infrastructure.entity.DocumentChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaDocumentChunkRepository extends JpaRepository<DocumentChunkEntity, UUID> {

    List<DocumentChunkEntity> findByKnowledgeDocumentId(UUID knowledgeDocumentId);

    @Query("SELECT d.documentChunkId FROM DocumentChunkEntity d WHERE d.knowledgeDocumentId = :knowledgeDocumentId")
    List<UUID> findIdsByKnowledgeDocumentId(@Param("knowledgeDocumentId") UUID knowledgeDocumentId);

    int countByKnowledgeDocumentId(UUID knowledgeDocumentId);

    @Modifying
    @Query("DELETE FROM DocumentChunkEntity d WHERE d.knowledgeDocumentId = :knowledgeDocumentId")
    void deleteByKnowledgeDocumentId(@Param("knowledgeDocumentId") UUID knowledgeDocumentId);
}