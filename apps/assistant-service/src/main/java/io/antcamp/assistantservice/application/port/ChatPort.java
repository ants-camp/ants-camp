package io.antcamp.assistantservice.application.port;

import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.RetrievedChunk;
import io.antcamp.assistantservice.domain.model.SourceReference;

import java.util.List;
import java.util.UUID;

public interface ChatPort {

    ChatMessage saveUserMessage(UUID chatSessionId, String content);

    ChatMessage saveBotResult(ChatMessage completedUserMessage, UUID chatSessionId, String content,
                              List<SourceReference> sources, BotResultContext context);

    ChatMessage saveErrorBotResult(ChatMessage completedUserMessage, UUID chatSessionId, String errorMessage);

    record BotResultContext(
            String userQuery,
            List<RetrievedChunk> retrievedChunks,
            String promptUsed,
            String llmModel,
            int latencyMs,
            int promptTokens,
            int completionTokens
    ) {}
}