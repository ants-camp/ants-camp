package io.antcamp.assistantservice.infrastructure.vector;

import io.antcamp.assistantservice.application.port.VectorStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpringAiVectorStoreAdapter implements VectorStorePort {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    // OpenAI 임베딩 호출 포함
    @IngestRetryPolicy.Retry
    @Override
    public void store(List<ChunkToStore> chunks) {
        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(
                        chunk.documentChunkId().toString(),
                        chunk.content(),
                        Map.of(
                                "documentChunkId", chunk.documentChunkId().toString(),
                                "knowledgeDocumentId", chunk.knowledgeDocumentId().toString(),
                                "title", chunk.title(),
                                "docType", chunk.docType()
                        )
                ))
                .toList();
        vectorStore.add(documents);
    }

    // pgvector 삭제 전용
    @IngestRetryPolicy.Retry
    @Override
    public void deleteByDocumentId(UUID documentId) {
        jdbcTemplate.update(
                "DELETE FROM vector_store WHERE metadata->>'knowledgeDocumentId' = ?",
                documentId.toString()
        );
    }
}