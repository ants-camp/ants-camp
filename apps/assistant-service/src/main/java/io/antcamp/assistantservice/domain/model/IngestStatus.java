package io.antcamp.assistantservice.domain.model;

public enum IngestStatus {
    PROCESSING,
    COMPLETED,
    FAILED,
    CLEANUP_PENDING // 삭제 대기
}