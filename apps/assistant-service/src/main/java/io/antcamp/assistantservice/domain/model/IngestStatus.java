package io.antcamp.assistantservice.domain.model;

public enum IngestStatus {
    PROCESSING,
    COMPLETED,
    FAILED,
    CLEANUP_PENDING, // 삭제 대기
    DELETED          // 정리 완료 후 soft-delete
}