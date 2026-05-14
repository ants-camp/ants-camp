package io.antcamp.notificationservice.application.dto.response;

import io.antcamp.notificationservice.domain.model.AlertSeverity;
import io.antcamp.notificationservice.domain.model.AlertSource;
import io.antcamp.notificationservice.domain.model.AlertStatus;
import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.model.ResolutionAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDetailResponse(
        UUID notificationId,
        String channelId,
        String job,
        AlertSource source,
        String deduplicationKey,
        AlertSeverity severity,
        AlertStatus status,
        String title,
        String content,
        String payload,
        String aiAnalysis,
        String slackMessageTs,
        ResolutionAction actionButton,
        String actionUserEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationDetailResponse of(Notification notification) {
        return new NotificationDetailResponse(
                notification.getNotificationId(),
                notification.getChannelId(),
                notification.getJob(),
                notification.getSource(),
                notification.getDeduplicationKey(),
                notification.getSeverity(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRawPayload(),
                notification.getAiAnalysis(),
                notification.getSlackMessageTs(),
                notification.getActionButton(),
                notification.getActionUserEmail(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
