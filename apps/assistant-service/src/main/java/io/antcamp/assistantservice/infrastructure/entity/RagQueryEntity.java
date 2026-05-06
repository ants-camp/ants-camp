package io.antcamp.assistantservice.infrastructure.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.RagQuery;
import io.antcamp.assistantservice.domain.model.RagQuerySource;
import io.antcamp.assistantservice.domain.model.RetrievedChunk;
import io.antcamp.assistantservice.infrastructure.util.JsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_rag_queries")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagQueryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rag_query_id", updatable = false, nullable = false)
    private UUID ragQueryId;

    @Column(name = "chat_message_id")
    private UUID chatMessageId;

    @Column(name = "user_query", nullable = false, columnDefinition = "TEXT")
    private String userQuery;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retrieved_chunks", nullable = false, columnDefinition = "jsonb")
    private String retrievedChunks;

    @Column(name = "prompt_used", nullable = false, columnDefinition = "TEXT")
    private String promptUsed;

    @Column(name = "llm_model", nullable = false, length = 50)
    private String llmModel;

    @Column(name = "llm_response", nullable = false, columnDefinition = "TEXT")
    private String llmResponse;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "top_k")
    private Integer topK;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 10)
    private RagQuerySource source;

    public static RagQueryEntity from(RagQuery domain) {
        return RagQueryEntity.builder()
                .ragQueryId(domain.getRagQueryId())
                .chatMessageId(domain.getChatMessageId())
                .userQuery(domain.getUserQuery())
                .retrievedChunks(JsonConverter.toJson(domain.getRetrievedChunks()))
                .promptUsed(domain.getPromptUsed())
                .llmModel(domain.getLlmModel())
                .llmResponse(domain.getLlmResponse())
                .latencyMs(domain.getLatencyMs())
                .promptTokens(domain.getPromptTokens())
                .completionTokens(domain.getCompletionTokens())
                .topK(domain.getTopK())
                .source(domain.getSource())
                .build();
    }

    public RagQuery toDomain() {
        List<RetrievedChunk> chunks = JsonConverter.fromJson(
                this.retrievedChunks, new TypeReference<>() {}
        );
        return RagQuery.restore(
                this.ragQueryId, this.chatMessageId, this.userQuery, chunks,
                this.promptUsed, this.llmModel, this.llmResponse,
                this.latencyMs, this.promptTokens, this.completionTokens
        );
    }
}