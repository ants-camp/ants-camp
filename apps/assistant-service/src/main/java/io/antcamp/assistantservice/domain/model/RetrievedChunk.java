package io.antcamp.assistantservice.domain.model;

import io.antcamp.assistantservice.domain.exception.InvalidInputException;

import java.util.UUID;

public record RetrievedChunk(UUID documentChunkId, double score, int rank, boolean used) {

    public RetrievedChunk {
        if (rank < 0) throw new InvalidInputException();
    }
}