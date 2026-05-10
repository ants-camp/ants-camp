package io.antcamp.assistantservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistant.response-cache")
public record ResponseCacheConfig(double similarityThreshold, int ttlHours) {
}