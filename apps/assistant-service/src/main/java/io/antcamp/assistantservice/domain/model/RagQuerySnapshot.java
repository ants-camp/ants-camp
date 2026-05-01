package io.antcamp.assistantservice.domain.model;

import java.util.UUID;

// Pairwise 비교 시 Run별 RAG 응답 조회용 읽기 모델
public record RagQuerySnapshot(UUID ragQueryId, String userQuery, String llmResponse) {}