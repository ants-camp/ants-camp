package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.dto.command.SendMessageCommand;
import io.antcamp.assistantservice.application.dto.result.SendMessageResult;
import io.antcamp.assistantservice.application.port.ChatPort;
import io.antcamp.assistantservice.application.port.LlmPort;
import io.antcamp.assistantservice.application.port.VectorStorePort;
import io.antcamp.assistantservice.domain.exception.SessionNotFoundException;
import io.antcamp.assistantservice.domain.model.*;
import io.antcamp.assistantservice.domain.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagApplicationService {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatPort chatPort;
    private final VectorStorePort vectorStorePort;
    private final LlmPort llmPort;

    private static final int TOP_K = 5;
    private static final String ERROR_RESPONSE = "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            본 서비스는 가상 머니를 기반으로 사용자가 실제 주식 데이터를 참고하여 투자 대회에 참가하고,
            대회 종료 시점의 수익률을 기준으로 순위를 산정하는 모의 주식 대회 플랫폼입니다.

            당신은 앤트캠프 주식 대회 플랫폼의 AI 어시스턴트입니다.
            아래의 참고 문서를 바탕으로 사용자 질문에 정확하고 친절하게 한국어로 답변하세요.
            참고 문서에 없는 내용은 "해당 정보를 찾을 수 없습니다"라고 답변하세요.

            [참고 문서]
            %s
            """;


    public SendMessageResult sendMessage(SendMessageCommand command) {
        ChatSession session = chatSessionRepository.findById(command.chatSessionId())
                .orElseThrow(SessionNotFoundException::new);
        if (!session.getUserId().equals(command.userId())) {
            throw new SessionNotFoundException();
        }

        List<ChatMessage> history = chatSessionRepository.findMessages(command.chatSessionId());
        ChatMessage userMessage = chatPort.saveUserMessage(command.chatSessionId(), command.content());

        SendMessageResult result = generateBotResponse(userMessage, buildLlmHistory(history));

        if (history.isEmpty()) {
            String title;
            try {
                title = llmPort.generateTitle(command.content());
            } catch (Exception e) {
                log.warn("제목 생성 실패, 첫 메시지로 대체: sessionId={}", command.chatSessionId(), e);
                title = command.content();
            }
            session.updateTitle(title);
            chatSessionRepository.save(session);
        }

        return result;
    }

    public void retryPendingMessage(ChatMessage pendingUserMessage) {
        List<LlmPort.HistoryMessage> llmHistory = chatSessionRepository.findMessages(pendingUserMessage.getChatSessionId())
                .stream()
                .filter(m -> !m.getChatMessageId().equals(pendingUserMessage.getChatMessageId()))
                .map(LlmPort.HistoryMessage::from)
                .toList();
        generateBotResponse(pendingUserMessage, llmHistory);
    }

    private SendMessageResult generateBotResponse(ChatMessage userMessage, List<LlmPort.HistoryMessage> llmHistory) {
        UUID chatSessionId = userMessage.getChatSessionId();

        List<VectorStorePort.SearchedChunk> searchedChunks;
        try {
            searchedChunks = vectorStorePort.search(userMessage.getContent(), TOP_K);
        } catch (Exception e) {
            log.warn("벡터 검색 실패, 빈 컨텍스트로 진행: sessionId={}", chatSessionId, e);
            searchedChunks = List.of();
        }

        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(buildChunksText(searchedChunks));
        long startTime = System.currentTimeMillis();
        LlmPort.LlmResult llmResult;
        try {
            llmResult = llmPort.chatAnswer(systemPrompt, userMessage.getContent(), llmHistory);
        } catch (Exception e) {
            log.error("LLM 호출 최종 실패, 오류 응답 저장: sessionId={}", chatSessionId, e);
            userMessage.complete();
            return SendMessageResult.from(chatPort.saveErrorBotResult(userMessage, chatSessionId, ERROR_RESPONSE));
        }
        int latencyMs = (int) (System.currentTimeMillis() - startTime);

        List<SourceReference> sources = buildSources(searchedChunks);
        ChatPort.BotResultContext ctx = new ChatPort.BotResultContext(
                userMessage.getContent(),
                buildRetrievedChunks(searchedChunks),
                systemPrompt,
                llmResult.modelName(),
                latencyMs,
                llmResult.promptTokens(),
                llmResult.completionTokens()
        );
        userMessage.complete();
        ChatMessage savedBotMessage = chatPort.saveBotResult(userMessage, chatSessionId, llmResult.content(), sources, ctx);

        log.info("RAG 응답 완료: sessionId={}, latencyMs={}", chatSessionId, latencyMs);
        return SendMessageResult.from(savedBotMessage);
    }

    private List<LlmPort.HistoryMessage> buildLlmHistory(List<ChatMessage> history) {
        return history.stream().map(LlmPort.HistoryMessage::from).toList();
    }

    private String buildChunksText(List<VectorStorePort.SearchedChunk> chunks) {
        if (chunks.isEmpty()) return "관련 문서 없음";
        return chunks.stream()
                .map(chunk -> "[%s (%s)]\n%s".formatted(chunk.title(), chunk.docType(), chunk.content()))
                .collect(Collectors.joining("\n\n"));
    }

    private List<SourceReference> buildSources(List<VectorStorePort.SearchedChunk> chunks) {
        return chunks.stream()
                .map(chunk -> new SourceReference(chunk.knowledgeDocumentId(), chunk.title(), chunk.docType()))
                .distinct()
                .toList();
    }

    private List<RetrievedChunk> buildRetrievedChunks(List<VectorStorePort.SearchedChunk> chunks) {
        int rank = 1;
        List<RetrievedChunk> result = new ArrayList<>();
        for (VectorStorePort.SearchedChunk chunk : chunks) {
            result.add(new RetrievedChunk(
                    chunk.documentChunkId(),
                    chunk.score() != null ? chunk.score() : 0.0,
                    rank++,
                    true
            ));
        }
        return result;
    }
}