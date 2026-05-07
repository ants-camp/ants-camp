package io.antcamp.assistantservice.domain.model;

import java.util.UUID;

public record RetrievedChunk(UUID documentChunkId, double score, int rank, boolean used) {

    public RetrievedChunk {
        if (rank < 0) throw new IllegalArgumentException("순위는 0 이상이어야 합니다.");
    }
}