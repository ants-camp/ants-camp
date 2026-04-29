package io.antcamp.assistantservice.domain.model;

import common.exception.ErrorCode;
import io.antcamp.assistantservice.domain.exception.InvalidDocumentException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class KnowledgeDocument {

    private UUID documentId;
    private String title;
    private DocType type;
    private String content;
    private IngestStatus ingestStatus;
    private LocalDateTime updatedAt;
    private String failureReason;
    private int retryCount;
    private LocalDateTime lastAttemptAt;

    public static KnowledgeDocument create(String title, DocType type, String content) {
        if (title == null || title.isBlank()) throw new InvalidDocumentException(ErrorCode.DOCUMENT_TITLE_BLANK);
        if (title.length() > 100) throw new InvalidDocumentException(ErrorCode.DOCUMENT_TITLE_TOO_LONG);
        if (content == null || content.isBlank()) throw new InvalidDocumentException(ErrorCode.DOCUMENT_CONTENT_BLANK);
        return KnowledgeDocument.builder()
                .title(title)
                .type(type)
                .content(content)
                .ingestStatus(IngestStatus.PROCESSING)
                .retryCount(0)
                .lastAttemptAt(LocalDateTime.now())
                .build();
    }

    public static KnowledgeDocument restore(UUID documentId, String title, DocType type,
                                            String content, IngestStatus ingestStatus, LocalDateTime updatedAt,
                                            String failureReason, int retryCount, LocalDateTime lastAttemptAt) {
        return KnowledgeDocument.builder()
                .documentId(documentId)
                .title(title)
                .type(type)
                .content(content)
                .ingestStatus(ingestStatus)
                .updatedAt(updatedAt)
                .failureReason(failureReason)
                .retryCount(retryCount)
                .lastAttemptAt(lastAttemptAt)
                .build();
    }

    public void update(String title, DocType type, String content) {
        if (title == null || title.isBlank()) throw new InvalidDocumentException(ErrorCode.DOCUMENT_TITLE_BLANK);
        if (content == null || content.isBlank()) throw new InvalidDocumentException(ErrorCode.DOCUMENT_CONTENT_BLANK);
        this.title = title;
        this.type = type;
        this.content = content;
        this.ingestStatus = IngestStatus.PROCESSING;
        this.failureReason = null;
        this.retryCount = 0;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.ingestStatus = IngestStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        this.ingestStatus = IngestStatus.FAILED;
        this.failureReason = reason;
        this.retryCount = this.retryCount + 1;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void markProcessing() {
        this.ingestStatus = IngestStatus.PROCESSING;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void markCleanupPending() {
        this.ingestStatus = IngestStatus.CLEANUP_PENDING;
    }

    public boolean isReconcilable(int maxRetry) {
        return retryCount < maxRetry && !isPermanentFailure();
    }

    private boolean isPermanentFailure() {
        return failureReason != null && failureReason.startsWith("PERMANENT");
    }
}