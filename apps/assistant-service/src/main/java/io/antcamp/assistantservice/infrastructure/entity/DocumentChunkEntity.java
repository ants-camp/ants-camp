package io.antcamp.assistantservice.infrastructure.entity;

import io.antcamp.assistantservice.domain.model.DocumentChunk;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_document_chunks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_chunk_id", updatable = false, nullable = false)
    private UUID documentChunkId;

    @Column(name = "knowledge_document_id", nullable = false)
    private UUID knowledgeDocumentId;

    @Column(name = "seq", nullable = false)
    private int seq;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    public static DocumentChunkEntity from(DocumentChunk domain) {
        return DocumentChunkEntity.builder()
                .documentChunkId(domain.getDocumentChunkId())
                .knowledgeDocumentId(domain.getKnowledgeDocumentId())
                .seq(domain.getSeq())
                .content(domain.getContent())
                .build();
    }

    public DocumentChunk toDomain() {
        return DocumentChunk.restore(
                this.documentChunkId, this.knowledgeDocumentId, this.seq, this.content
        );
    }
}