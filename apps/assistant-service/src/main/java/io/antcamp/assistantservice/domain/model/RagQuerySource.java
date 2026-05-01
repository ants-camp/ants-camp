package io.antcamp.assistantservice.domain.model;

public enum RagQuerySource {
    CHAT,   // 실제 사용자 채팅에서 발생한 RAG
    EVAL    // 평가 파이프라인에서 발생한 합성 RAG
}