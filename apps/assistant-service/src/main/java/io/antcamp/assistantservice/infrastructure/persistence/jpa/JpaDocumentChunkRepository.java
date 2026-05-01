package io.antcamp.assistantservice.infrastructure.persistence.jpa;

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

    // COMPLETED 상태 문서의 청크만 샘플링 (인제스트 완료된 것만)
    @Query(value = """
            SELECT c.* FROM p_document_chunks c
            JOIN p_documents d ON c.knowledge_document_id = d.document_id
            WHERE d.ingest_status = 'COMPLETED' AND d.deleted_at IS NULL
            ORDER BY RANDOM()
            LIMIT :count
            """, nativeQuery = true)
    List<DocumentChunkEntity> findRandomChunks(@Param("count") int count);
}