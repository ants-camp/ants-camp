package io.antcamp.assistantservice.infrastructure.llm;

import io.antcamp.assistantservice.application.port.LlmPort;
import io.antcamp.assistantservice.domain.model.Role;
import io.antcamp.assistantservice.infrastructure.config.LlmConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAiChatAdapter implements LlmPort {

    private final OpenAiChatModel openAiChatModel;
    private final AnthropicChatModel anthropicChatModel;
    private final LlmConfig llmConfig;

    public OpenAiChatAdapter(OpenAiChatModel openAiChatModel,
                              @Autowired(required = false) AnthropicChatModel anthropicChatModel,
                              LlmConfig llmConfig) {
        this.openAiChatModel = openAiChatModel;
        this.anthropicChatModel = anthropicChatModel;
        this.llmConfig = llmConfig;
    }

    /**
     *재시도 조건 (retryFor)
     *   - IOException — 네트워크 단절, 타임아웃 등 I/O 오류
     *   - HttpServerErrorException — 5xx 응답 (OpenAI 서버 오류)
     *   - 4xx (잘못된 요청 등)는 재시도 대상이 아님 — 재시도해도 의미 없으므로 제외
     *   - 1초 > 2초 > 4초 > 8초
     */
    @Retryable(
            retryFor = {IOException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    @Override
    public String generateQuestion(String chunkContent) {
        ChatResponse response = openAiChatModel.call(new Prompt(List.of(
                new SystemMessage("""
                        당신은 주식 모의 투자 대회 플랫폼의 고객 응대 전문가입니다.
                        아래 문서 내용을 읽고, 실제 사용자가 고객센터에 물어볼 법한 자연스러운 한국어 질문을 1개만 생성하세요.
                        질문 텍스트만 반환하세요. 다른 텍스트는 포함하지 마세요.
                        """),
                new UserMessage(chunkContent)
        )));
        return response.getResult().getOutput().getText().trim();
    }

    @Retryable(
            retryFor = {IOException.class, HttpServerErrorException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    @Override
    public String generateTitle(String userMessage) {
        ChatResponse response = openAiChatModel.call(new Prompt(List.of(
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

        // ANTHROPIC api 호출
        String modelName = llmConfig.modelName();
        Provider provider = Provider.from(modelName);

        Prompt prompt = provider == Provider.ANTHROPIC
                ? new Prompt(messages, AnthropicChatOptions.builder().model(modelName).build())
                : new Prompt(messages, OpenAiChatOptions.builder().model(modelName).build());

        ChatResponse response = provider == Provider.ANTHROPIC && anthropicChatModel != null
                ? anthropicChatModel.call(prompt)
                : openAiChatModel.call(prompt);

        // 결과 파싱
        String content = response.getResult().getOutput().getText();
        Usage usage = response.getMetadata().getUsage();

        int promptTokens = (usage != null && usage.getPromptTokens() != null) ? usage.getPromptTokens() : 0;
        int completionTokens = (usage != null && usage.getCompletionTokens() != null) ? usage.getCompletionTokens() : 0;

        return new LlmResult(content, llmConfig.modelName(), promptTokens, completionTokens);
    }
}