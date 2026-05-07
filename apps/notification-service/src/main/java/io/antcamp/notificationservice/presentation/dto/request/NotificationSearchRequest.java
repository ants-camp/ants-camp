package io.antcamp.notificationservice.presentation.dto.request;

import io.antcamp.notificationservice.domain.model.AlertSeverity;
import io.antcamp.notificationservice.domain.model.AlertSource;
import io.antcamp.notificationservice.domain.model.AlertStatus;
import io.antcamp.notificationservice.domain.repository.NotificationSearchCriteria;
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
    public NotificationSearchCriteria toCriteria() {
        return new NotificationSearchCriteria(
                status, severity, source, job, from, to, actionUserEmail, handledOnly
        );
    }
}
