package io.antcamp.assistantservice.infrastructure.persistence;

import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.exception.DocumentNotFoundException;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.domain.model.DocumentChunk;
import io.antcamp.assistantservice.domain.model.IngestStatus;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;
import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import io.antcamp.assistantservice.infrastructure.entity.DocumentChunkEntity;
import io.antcamp.assistantservice.infrastructure.entity.KnowledgeDocumentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class KnowledgeDocumentRepositoryImpl implements KnowledgeDocumentRepository {

    private final JpaKnowledgeDocumentRepository jpaRepository;
    private final JpaDocumentChunkRepository jpaChunkRepository;
    private final KnowledgeDocumentQueryRepository documentQueryRepository;

    @Override
    public KnowledgeDocument save(KnowledgeDocument document) {
        return jpaRepository.save(KnowledgeDocumentEntity.from(document)).toDomain();
    }

    @Override
    public Optional<KnowledgeDocument> findById(UUID documentId) {
        return jpaRepository.findById(documentId).map(KnowledgeDocumentEntity::toDomain);
    }

    @Override
    public Optional<KnowledgeDocument> findAccessibleById(UUID documentId) {
        return documentQueryRepository.findAccessibleById(documentId)
                .map(KnowledgeDocumentEntity::toDomain);
    }

    @Override
    public CursorSlice<KnowledgeDocument, LocalDateTime> findDocuments(
            DocType type, String title, String keyword, LocalDateTime lastUpdatedAt, int pageSize) {
        CursorSlice<KnowledgeDocumentEntity, LocalDateTime> slice =
                documentQueryRepository.findDocuments(type, title, keyword, lastUpdatedAt, pageSize);

        return new CursorSlice<>(
                slice.items().stream().map(KnowledgeDocumentEntity::toDomain).toList(),
                slice.hasNext(),
                slice.nextCursor()
        );
    }

    @Override
    public void deleteById(UUID documentId) {
        KnowledgeDocumentEntity entity = jpaRepository.findById(documentId)
                .orElseThrow(DocumentNotFoundException::new);
        entity.softDelete("SYSTEM");
        jpaRepository.save(entity);
    }

    @Override
    public List<DocumentChunk> saveChunks(List<DocumentChunk> chunks) {
        return jpaChunkRepository.saveAll(chunks.stream().map(DocumentChunkEntity::from).toList())
                .stream().map(DocumentChunkEntity::toDomain).toList();
    }

    @Override
    public void deleteChunksByDocumentId(UUID documentId) {
        jpaChunkRepository.deleteByKnowledgeDocumentId(documentId);
    }

    @Override
    public int countChunksByDocumentId(UUID documentId) {
        return jpaChunkRepository.countByKnowledgeDocumentId(documentId);
    }

    @Override
    public List<KnowledgeDocument> findReconcileTargets(LocalDateTime processingThreshold, int maxRetry) {
        return documentQueryRepository.findReconcileTargets(processingThreshold, maxRetry)
                .stream()
                .map(KnowledgeDocumentEntity::toDomain)
                .filter(doc -> doc.isReconcilable(maxRetry))
                .toList();
    }

    @Override
    public List<UUID> findCleanupPendingIds() {
        return jpaRepository.findIdsByIngestStatus(IngestStatus.CLEANUP_PENDING);
    }
}