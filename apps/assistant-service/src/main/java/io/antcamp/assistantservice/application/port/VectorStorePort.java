package io.antcamp.assistantservice.application.port;

import java.util.List;
import java.util.UUID;

public interface VectorStorePort {

    void store(List<ChunkToStore> chunks);

    void deleteByDocumentId(UUID documentId);

    record ChunkToStore(UUID documentChunkId, UUID knowledgeDocumentId, String title, String docType, String content) {
    }
}