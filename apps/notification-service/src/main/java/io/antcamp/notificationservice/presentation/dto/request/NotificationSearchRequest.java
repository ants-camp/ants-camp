package io.antcamp.notificationservice.presentation.dto.request;

import io.antcamp.notificationservice.application.dto.query.NotificationSearchQuery;
import io.antcamp.notificationservice.domain.model.AlertSeverity;
import io.antcamp.notificationservice.domain.model.AlertSource;
import io.antcamp.notificationservice.domain.model.AlertStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record NotificationSearchRequest(
        AlertStatus status,
        AlertSeverity severity,
        AlertSource source,
        String job,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        String actionUserEmail,
        Boolean handledOnly
) {
    public NotificationSearchQuery toQuery(int page) {
        return new NotificationSearchQuery(
                status, severity, source, job, from, to, actionUserEmail, handledOnly, page
        );
    }
}
