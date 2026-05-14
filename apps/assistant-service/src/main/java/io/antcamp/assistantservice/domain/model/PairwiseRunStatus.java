package io.antcamp.assistantservice.domain.model;

public enum PairwiseRunStatus {
    PENDING,    // POST 수신, 비동기 처리 시작 전
    RUNNING,    // LLM 판정 진행 중
    COMPLETED,  // 모든 판정 완료
    FAILED      // 공통 질문 없음 또는 처리 중 오류
}