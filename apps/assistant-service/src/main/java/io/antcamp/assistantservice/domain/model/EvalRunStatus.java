package io.antcamp.assistantservice.domain.model;

public enum EvalRunStatus {
    PENDING,    // 생성됨, 처리 대기
    RUNNING,    // 비동기 처리 중
    COMPLETED,  // 모든 채점 완료
    FAILED      // 처리 중 오류 발생
}