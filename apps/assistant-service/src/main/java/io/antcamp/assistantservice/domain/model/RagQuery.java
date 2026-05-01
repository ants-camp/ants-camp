package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class RagQuery {

    private UUID ragQueryId;
    private UUID chatMessageId;
    private String userQuery;
    private List<RetrievedChunk> retrievedChunks;
    private String promptUsed;
    private String llmModel;
    private String llmResponse;
    private Integer latencyMs;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer topK;
    private RagQuerySource source;

    public static RagQuery create(UUID chatMessageId, String userQuery, List<RetrievedChunk> retrievedChunks,
                                  String promptUsed, String llmModel, String llmResponse,
                                  Integer latencyMs, Integer promptTokens, Integer completionTokens) {
        return RagQuery.builder()
                .chatMessageId(chatMessageId)
                .userQuery(userQuery)
                .retrievedChunks(retrievedChunks != null ? retrievedChunks : List.of())
                .promptUsed(promptUsed)
                .llmModel(llmModel)
                .llmResponse(llmResponse)
                .latencyMs(latencyMs)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .source(RagQuerySource.CHAT)
                .build();
    }

    // 평가 파이프라인 전용
    public static RagQuery createForEval(String userQuery, List<RetrievedChunk> retrievedChunks,
                                         String promptUsed, String llmModel, String llmResponse,
                                         Integer latencyMs, Integer promptTokens, Integer completionTokens,
                                         Integer topK) {
        return RagQuery.builder()
                .chatMessageId(null)
                .userQuery(userQuery)
                .retrievedChunks(retrievedChunks != null ? retrievedChunks : List.of())
                .promptUsed(promptUsed)
                .llmModel(llmModel)
                .llmResponse(llmResponse)
                .latencyMs(latencyMs)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .topK(topK)
                .source(RagQuerySource.EVAL)
                .build();
    }

    public static RagQuery restore(UUID ragQueryId, UUID chatMessageId, String userQuery,
                                   List<RetrievedChunk> retrievedChunks, String promptUsed,
                                   String llmModel, String llmResponse, Integer latencyMs,
                                   Integer promptTokens, Integer completionTokens) {
        return RagQuery.builder()
                .ragQueryId(ragQueryId)
                .chatMessageId(chatMessageId)
                .userQuery(userQuery)
                .retrievedChunks(retrievedChunks != null ? retrievedChunks : List.of())
                .promptUsed(promptUsed)
                .llmModel(llmModel)
                .llmResponse(llmResponse)
                .latencyMs(latencyMs)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .build();
    }
}