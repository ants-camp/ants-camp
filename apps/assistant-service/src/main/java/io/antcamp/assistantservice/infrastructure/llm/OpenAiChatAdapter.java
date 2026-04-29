package io.antcamp.assistantservice.infrastructure.llm;

import io.antcamp.assistantservice.application.port.LlmPort;
import io.antcamp.assistantservice.domain.model.Role;
import io.antcamp.assistantservice.infrastructure.config.LlmConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiChatAdapter implements LlmPort {

    private final ChatModel chatModel;
    private final LlmConfig llmConfig;

    @Retryable(
            retryFor = {IOException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    @Override
    public String generateTitle(String userMessage) {
        ChatResponse response = chatModel.call(new Prompt(List.of(
                new SystemMessage("사용자의 첫 번째 메시지를 읽고 대화 제목을 20자 이내 한국어로 생성하세요. 제목만 반환하세요."),
                new UserMessage(userMessage)
        )));
        return response.getResult().getOutput().getText().trim();
    }

    @Retryable(
            retryFor = {IOException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    @Override
    public LlmResult chatAnswer(String systemPrompt, String userMessage, List<HistoryMessage> history) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        for (HistoryMessage msg : history) {
            messages.add(msg.role() == Role.USER
                    ? new UserMessage(msg.content())
                    : new AssistantMessage(msg.content()));
        }
        messages.add(new UserMessage(userMessage));

        // OpenAI API 호출
        ChatResponse response = chatModel.call(new Prompt(messages));

        // 결과 파싱
        String content = response.getResult().getOutput().getText();
        Usage usage = response.getMetadata().getUsage();

        int promptTokens = (usage != null && usage.getPromptTokens() != null) ? usage.getPromptTokens() : 0;
        int completionTokens = (usage != null && usage.getCompletionTokens() != null) ? usage.getCompletionTokens() : 0;

        return new LlmResult(content, llmConfig.modelName(), promptTokens, completionTokens);
    }
}