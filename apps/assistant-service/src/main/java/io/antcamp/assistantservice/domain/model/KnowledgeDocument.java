package io.antcamp.assistantservice.domain.model;

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
        if (title == null || title.isBlank()) throw new IllegalArgumentException("문서 제목은 비어있을 수 없습니다.");
        if (title.length() > 100) throw new IllegalArgumentException("문서 제목은 100자를 초과할 수 없습니다.");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("문서 내용은 비어있을 수 없습니다.");
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
        if (title == null || title.isBlank()) throw new IllegalArgumentException("문서 제목은 비어있을 수 없습니다.");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("문서 내용은 비어있을 수 없습니다.");
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

    public void markDeleted() {
        this.ingestStatus = IngestStatus.DELETED;
    }


    public boolean isReconcilable(int maxRetry) {
        return retryCount < maxRetry && !isPermanentFailure();
    }

    private boolean isPermanentFailure() {
        return failureReason != null && failureReason.startsWith("PERMANENT");
    }
}