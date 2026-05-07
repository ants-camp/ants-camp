package io.antcamp.assistantservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistant.llm")
public record LlmConfig(String modelName) {
}