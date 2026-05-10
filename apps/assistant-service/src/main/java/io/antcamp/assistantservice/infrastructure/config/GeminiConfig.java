package io.antcamp.assistantservice.infrastructure.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.ai.vertex.ai.gemini", name = "api-key")
public class GeminiConfig {

    // Gemini의 OpenAI 호환 엔드포인트를 사용
    @Bean("geminiChatModel")
    public ChatModel geminiChatModel(
            @Value("${spring.ai.vertex.ai.gemini.api-key}") String apiKey) {
        OpenAiApi geminiApi = OpenAiApi.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/")
                .apiKey(apiKey)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(geminiApi)
                .build();
    }
}