package io.antcamp.assistantservice.infrastructure.persistence;

import io.antcamp.assistantservice.application.port.ChatPort;
import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.RagQuery;
import io.antcamp.assistantservice.domain.model.SourceReference;
import io.antcamp.assistantservice.infrastructure.entity.ChatMessageEntity;
import io.antcamp.assistantservice.infrastructure.entity.RagQueryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatPersistenceAdapter implements ChatPort {

    private final JpaChatMessageRepository messageRepository;
    private final JpaRagQueryRepository ragQueryRepository;

    @Transactional
    @Override
    public ChatMessage saveUserMessage(UUID chatSessionId, String content) {
        int nextSeq = messageRepository.findMaxSeqForUpdate(chatSessionId) + 1;
        return messageRepository.save(
                ChatMessageEntity.from(ChatMessage.createUserMessage(chatSessionId, content, nextSeq))
        ).toDomain();
    }

    // 완료된 유저 메시지 + 봇 메시지 + RAG 쿼리를 단일 트랜잭션으로 저장
    @Transactional
    @Override
    public ChatMessage saveBotResult(ChatMessage completedUserMessage, UUID chatSessionId, String content,
                                     List<SourceReference> sources, BotResultContext ctx) {
        messageRepository.save(ChatMessageEntity.from(completedUserMessage));

        int nextSeq = messageRepository.findMaxSeqForUpdate(chatSessionId) + 1;
        ChatMessageEntity botEntity = messageRepository.save(
                ChatMessageEntity.from(ChatMessage.createBotMessage(chatSessionId, content, nextSeq, sources))
        );
        ragQueryRepository.save(RagQueryEntity.from(RagQuery.create(
                botEntity.getChatMessageId(), ctx.userQuery(), ctx.retrievedChunks(), ctx.promptUsed(),
                ctx.llmModel(), content, ctx.latencyMs(), ctx.promptTokens(), ctx.completionTokens()
        )));
        return botEntity.toDomain();
    }

    // 완료된 유저 메시지 + 오류 봇 메시지를 단일 트랜잭션으로 저장 (RAG 쿼리 없음)
    @Transactional
    @Override
    public ChatMessage saveErrorBotResult(ChatMessage completedUserMessage, UUID chatSessionId, String errorMessage) {
        messageRepository.save(ChatMessageEntity.from(completedUserMessage));

        int nextSeq = messageRepository.findMaxSeqForUpdate(chatSessionId) + 1;
        return messageRepository.save(
                ChatMessageEntity.from(ChatMessage.createBotMessage(chatSessionId, errorMessage, nextSeq, List.of()))
        ).toDomain();
    }
}