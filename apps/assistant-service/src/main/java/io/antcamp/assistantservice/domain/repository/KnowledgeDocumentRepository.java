package io.antcamp.assistantservice.domain.repository;

import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.domain.model.DocumentChunk;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeDocumentRepository {

    KnowledgeDocument save(KnowledgeDocument document);

    Optional<KnowledgeDocument> findById(UUID documentId);

    Optional<KnowledgeDocument> findAccessibleById(UUID documentId);

    CursorSlice<KnowledgeDocument, LocalDateTime> findDocuments(DocType type, String title, String keyword, LocalDateTime lastUpdatedAt, int pageSize);

    void deleteById(UUID documentId);

    // 청크 일괄 작업 — Aggregate Root를 통한 단일 진입점
    List<DocumentChunk> saveChunks(List<DocumentChunk> chunks);

    void deleteChunksByDocumentId(UUID documentId);

    int countChunksByDocumentId(UUID documentId);

    // 인제스트 상태 관리
    List<KnowledgeDocument> findReconcileTargets(LocalDateTime processingThreshold, int maxRetry);

    // CleanupReconciler 대상 조회
    List<UUID> findCleanupPendingIds();
}