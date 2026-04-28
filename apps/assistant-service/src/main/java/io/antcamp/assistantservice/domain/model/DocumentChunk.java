package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class DocumentChunk {

    private UUID documentChunkId;
    private UUID knowledgeDocumentId;
    private int seq;
    private String content;

    public static DocumentChunk create(UUID knowledgeDocumentId, int seq, String content) {
        return DocumentChunk.builder()
                .documentChunkId(UUID.randomUUID())
                .knowledgeDocumentId(knowledgeDocumentId)
                .seq(seq)
                .content(content)
                .build();
    }

    public static DocumentChunk restore(UUID documentChunkId, UUID knowledgeDocumentId, int seq, String content) {
        return DocumentChunk.builder()
                .documentChunkId(documentChunkId)
                .knowledgeDocumentId(knowledgeDocumentId)
                .seq(seq)
                .content(content)
                .build();
    }
}