package io.antcamp.assistantservice.infrastructure.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.domain.model.IngestStatus;
import io.antcamp.assistantservice.infrastructure.entity.KnowledgeDocumentEntity;
import io.antcamp.assistantservice.infrastructure.entity.QKnowledgeDocumentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class KnowledgeDocumentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CursorSlice<KnowledgeDocumentEntity, LocalDateTime> findDocuments(
            DocType type, String title, String keyword, LocalDateTime lastUpdatedAt, int pageSize) {
        QKnowledgeDocumentEntity doc = QKnowledgeDocumentEntity.knowledgeDocumentEntity;

        List<KnowledgeDocumentEntity> rows = queryFactory
                .selectFrom(doc)
                .where(
                        typeEq(doc, type),
                        titleContains(doc, title),
                        keywordContains(doc, keyword),
                        cursorBefore(doc, lastUpdatedAt),
                        doc.ingestStatus.ne(IngestStatus.CLEANUP_PENDING)
                )
                .orderBy(doc.updatedAt.desc())
                .limit(pageSize + 1L)
                .fetch();

        boolean hasNext = rows.size() > pageSize;
        List<KnowledgeDocumentEntity> page = hasNext ? rows.subList(0, pageSize) : rows;
        LocalDateTime nextCursor = page.isEmpty() ? null : page.get(page.size() - 1).getUpdatedAt();

        return new CursorSlice<>(page, hasNext, nextCursor);
    }

    public Optional<KnowledgeDocumentEntity> findAccessibleById(UUID documentId) {
        QKnowledgeDocumentEntity doc = QKnowledgeDocumentEntity.knowledgeDocumentEntity;

        return Optional.ofNullable(
                queryFactory.selectFrom(doc)
                        .where(doc.documentId.eq(documentId)
                                .and(doc.ingestStatus.ne(IngestStatus.CLEANUP_PENDING)))
                        .fetchOne()
        );
    }

    public List<KnowledgeDocumentEntity> findReconcileTargets(LocalDateTime processingThreshold, int maxRetry) {
        QKnowledgeDocumentEntity doc = QKnowledgeDocumentEntity.knowledgeDocumentEntity;

        return queryFactory
                .selectFrom(doc)
                .where(
                        processingStuck(doc, processingThreshold)
                                .or(failedAndRetryable(doc, maxRetry))
                )
                .fetch();
    }

    private BooleanExpression typeEq(QKnowledgeDocumentEntity doc, DocType type) {
        return type != null ? doc.type.eq(type) : null;
    }

    private BooleanExpression titleContains(QKnowledgeDocumentEntity doc, String title) {
        return (title != null && !title.isBlank()) ? doc.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression keywordContains(QKnowledgeDocumentEntity doc, String keyword) {
        return (keyword != null && !keyword.isBlank())
                ? doc.title.containsIgnoreCase(keyword).or(doc.content.containsIgnoreCase(keyword))
                : null;
    }

    private BooleanExpression cursorBefore(QKnowledgeDocumentEntity doc, LocalDateTime lastUpdatedAt) {
        return lastUpdatedAt != null ? doc.updatedAt.lt(lastUpdatedAt) : null;
    }

    private BooleanExpression processingStuck(QKnowledgeDocumentEntity doc, LocalDateTime processingThreshold) {
        return doc.ingestStatus.eq(IngestStatus.PROCESSING)
                .and(doc.lastAttemptAt.isNull().or(doc.lastAttemptAt.lt(processingThreshold)));
    }

    private BooleanExpression failedAndRetryable(QKnowledgeDocumentEntity doc, int maxRetry) {
        return doc.ingestStatus.eq(IngestStatus.FAILED).and(doc.retryCount.lt(maxRetry));
    }
}