package io.antcamp.notificationservice.application.dto.response;

import io.antcamp.notificationservice.domain.model.AlertSeverity;
import io.antcamp.notificationservice.domain.model.AlertSource;
import io.antcamp.notificationservice.domain.model.AlertStatus;
import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.model.ResolutionAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationSummaryResponse(
        UUID notificationId,
        String job,
        AlertSource source,
        AlertSeverity severity,
        AlertStatus status,
        String title,
        ResolutionAction actionButton,
        String actionUserEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationSummaryResponse of(Notification notification) {
        return new NotificationSummaryResponse(
                notification.getNotificationId(),
                notification.getJob(),
                notification.getSource(),
                notification.getSeverity(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getActionButton(),
                notification.getActionUserEmail(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
