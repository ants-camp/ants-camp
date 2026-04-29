package io.antcamp.assistantservice.application.port;

import io.antcamp.assistantservice.domain.model.DocumentChunk;

import java.util.List;
import java.util.UUID;

public interface VectorStorePort {

    void store(List<ChunkToStore> chunks);

    List<SearchedChunk> search(String query, int topK);

    /** 문서 ID 기준으로 벡터 스토어에서 모든 청크 삭제 — 재인제스트 멱등성 보장 */
    void deleteByDocumentId(UUID documentId);

    record ChunkToStore(UUID documentChunkId, UUID knowledgeDocumentId, String title, String docType, String content) {
        public static ChunkToStore from(DocumentChunk chunk, String title, String docType) {
            return new ChunkToStore(chunk.getDocumentChunkId(), chunk.getKnowledgeDocumentId(), title, docType, chunk.getContent());
        }
    }

    record SearchedChunk(UUID documentChunkId, UUID knowledgeDocumentId, String title, String docType,
                         String content, Double score) {
    }
}