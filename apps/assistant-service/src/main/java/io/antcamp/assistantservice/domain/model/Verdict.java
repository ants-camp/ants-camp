package io.antcamp.assistantservice.domain.model;

// Pairwise 비교 판정 결과
public enum Verdict {
    A_WINS,         // Run A의 응답이 더 좋음
    B_WINS,         // Run B의 응답이 더 좋음
    TIE             // 비슷한 수준
}