package io.antcamp.assistantservice.infrastructure.llm;

import io.antcamp.assistantservice.application.port.EvalRagPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EvalRagAdapter implements EvalRagPort {

    private final Map<Provider, ChatModel> modelRegistry;

    public EvalRagAdapter(@Qualifier("openAiChatModel") OpenAiChatModel openAiModel,
                          @Autowired(required = false) AnthropicChatModel anthropicModel,
                          @Autowired(required = false) @Qualifier("geminiChatModel") ChatModel geminiModel) {
        Map<Provider, ChatModel> registry = new EnumMap<>(Provider.class);
        registry.put(Provider.OPENAI, openAiModel);
        if (anthropicModel != null) registry.put(Provider.ANTHROPIC, anthropicModel);
        if (geminiModel != null) registry.put(Provider.GEMINI, geminiModel);
        this.modelRegistry = Collections.unmodifiableMap(registry);
    }

    @Override
    public EvalRagResult generate(String ragModel, String systemPrompt, String question) {
        Provider provider = Provider.from(ragModel);
        ChatModel model = modelRegistry.get(provider);
        if (model == null) {
            throw new IllegalArgumentException("등록되지 않은 RAG 모델 provider: ragModel=" + ragModel);
        }

        Prompt prompt = buildPrompt(provider, ragModel, systemPrompt, question);
        ChatResponse response = model.call(prompt);

        var llmResult = response.getResult();
        if (llmResult == null || llmResult.getOutput() == null) {
            throw new IllegalStateException("LLM 응답이 비어 있습니다: ragModel=" + ragModel);
        }
        String content = llmResult.getOutput().getText();
        Usage usage = response.getMetadata().getUsage();
        int promptTokens = (usage != null && usage.getPromptTokens() != null) ? usage.getPromptTokens() : 0;
        int completionTokens = (usage != null && usage.getCompletionTokens() != null) ? usage.getCompletionTokens() : 0;

        return new EvalRagResult(content, ragModel, promptTokens, completionTokens);
    }

    private Prompt buildPrompt(Provider provider, String ragModel, String systemPrompt, String question) {
        List<Message> messages = List.of(new SystemMessage(systemPrompt), new UserMessage(question));
        return switch (provider) {
            case ANTHROPIC -> new Prompt(messages,
                    AnthropicChatOptions.builder().model(ragModel).build());
            default -> new Prompt(messages,
                    OpenAiChatOptions.builder().model(ragModel).build());
        };
    }
}