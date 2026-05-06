package io.antcamp.assistantservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.domain.model.IngestStatus;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_documents")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id", updatable = false, nullable = false)
    private UUID documentId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DocType type;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "ingest_status", nullable = false, length = 20)
    private IngestStatus ingestStatus;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    public void markDeleted() {
        this.ingestStatus = IngestStatus.DELETED;
    }

    public static KnowledgeDocumentEntity from(KnowledgeDocument domain) {
        return KnowledgeDocumentEntity.builder()
                .documentId(domain.getDocumentId())
                .title(domain.getTitle())
                .type(domain.getType())
                .content(domain.getContent())
                .ingestStatus(domain.getIngestStatus())
                .failureReason(domain.getFailureReason())
                .retryCount(domain.getRetryCount())
                .lastAttemptAt(domain.getLastAttemptAt())
                .build();
    }

    public KnowledgeDocument toDomain() {
        return KnowledgeDocument.restore(
                this.documentId, this.title, this.type,
                this.content, this.ingestStatus, this.getUpdatedAt(),
                this.failureReason, this.retryCount, this.lastAttemptAt
        );
    }
}