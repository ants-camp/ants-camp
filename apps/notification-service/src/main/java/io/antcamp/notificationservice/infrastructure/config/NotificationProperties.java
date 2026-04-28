package io.antcamp.notificationservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(List<String> infrastructureJobs) {
}