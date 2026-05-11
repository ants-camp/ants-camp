package io.antcamp.notificationservice.domain.model;

import io.antcamp.notificationservice.domain.exception.InvalidInputException;
import io.antcamp.notificationservice.domain.exception.NotificationException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Notification {

    private UUID notificationId;
    private final String channelId;
    private final String job;
    private final AlertSource source;
    private final String deduplicationKey;
    private final AlertSeverity severity;
    private final String title;
    private final String content;
    private final String rawPayload;   // Prometheus alert 원본 JSON
    private final String aiAnalysis;   // LLM 분석 결과
    private AlertStatus status;
    private String slackMessageTs;
    private ResolutionAction actionButton;
    private String actionUserEmail;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static Notification restore(
            UUID notificationId,
            String channelId,
            String job,
            AlertSource source,
            String deduplicationKey,
            AlertSeverity severity,
            String title,
            String content,
            String rawPayload,
            String aiAnalysis,
            AlertStatus status,
            String slackMessageTs,
            ResolutionAction actionButton,
            String actionUserEmail,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return Notification.builder()
                .notificationId(notificationId)
                .channelId(channelId)
                .job(job)
                .source(source)
                .deduplicationKey(deduplicationKey)
                .severity(severity)
                .title(title)
                .content(content)
                .rawPayload(rawPayload)
                .aiAnalysis(aiAnalysis)
                .status(status)
                .slackMessageTs(slackMessageTs)
                .actionButton(actionButton)
                .actionUserEmail(actionUserEmail)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Notification create(
            String channelId,
            String job,
            AlertSource source,
            String deduplicationKey,
            AlertSeverity severity,
            String title,
            String content,
            String rawPayload,
            String aiAnalysis
    ) {
        validateChannelId(channelId);
        validateDeduplicationKey(deduplicationKey);
        validateTitle(title);
        validateContent(content);

        LocalDateTime now = LocalDateTime.now();
        return Notification.builder()
                .channelId(channelId)
                .job(job)
                .source(source)
                .deduplicationKey(deduplicationKey)
                .severity(severity)
                .title(title)
                .content(content)
                .rawPayload(rawPayload)
                .aiAnalysis(aiAnalysis)
                .status(AlertStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markAsSent(String slackMessageTs) {
        if (this.status != AlertStatus.PENDING) {
            throw NotificationException.invalidState();
        }
        this.status = AlertStatus.SENT;
        this.slackMessageTs = slackMessageTs;
    }

    public void markAsFailed() {
        if (this.status != AlertStatus.PENDING) {
            throw NotificationException.invalidState();
        }
        this.status = AlertStatus.FAILED;
    }

    public void recordAction(ResolutionAction button, String userEmail) {
        if (this.status != AlertStatus.SENT) {
            throw NotificationException.invalidState();
        }
        if (this.actionButton != null) {
            throw NotificationException.alreadyHandled();
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw NotificationException.invalidField();
        }
        this.actionButton = button;
        this.actionUserEmail = userEmail;
    }

    public void markActionFailed() {
        if (this.status != AlertStatus.SENT || this.actionButton == null) {
            throw NotificationException.invalidState();
        }
        this.status = AlertStatus.ACTION_FAILED;
    }

    private static void validateChannelId(String channelId) {
        if (channelId == null || channelId.isBlank()) throw new InvalidInputException();
        if (channelId.length() > 100) throw new InvalidInputException();
    }

    private static void validateDeduplicationKey(String deduplicationKey) {
        if (deduplicationKey == null || deduplicationKey.isBlank()) throw new InvalidInputException();
        if (deduplicationKey.length() > 255) throw new InvalidInputException();
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) throw new InvalidInputException();
        if (title.length() > 255) throw new InvalidInputException();
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) throw new InvalidInputException();
    }
}
