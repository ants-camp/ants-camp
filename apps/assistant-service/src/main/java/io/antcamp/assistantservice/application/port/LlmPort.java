package io.antcamp.assistantservice.application.port;

import io.antcamp.assistantservice.domain.model.ChatMessage;
import io.antcamp.assistantservice.domain.model.Role;

import java.util.List;

public interface LlmPort {

    record LlmResult(String content, String modelName, int promptTokens, int completionTokens) {}

    record HistoryMessage(Role role, String content) {
        public static HistoryMessage from(ChatMessage message) {
            return new HistoryMessage(message.getRole(), message.getContent());
        }
    }

    LlmResult chatAnswer(String systemPrompt, String userMessage, List<HistoryMessage> history);

    String generateTitle(String userMessage);

    // 지식 문서 청크로부터 평가용 질문 1개 생성
    String generateQuestion(String chunkContent);
}