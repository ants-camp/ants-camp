package io.antcamp.notificationservice.application.dto.query;

import io.antcamp.notificationservice.domain.model.AlertSeverity;
import io.antcamp.notificationservice.domain.model.AlertSource;
import io.antcamp.notificationservice.domain.model.AlertStatus;
import io.antcamp.notificationservice.domain.repository.NotificationSearchCriteria;
import io.antcamp.notificationservice.domain.repository.PagingRequest;

import java.time.LocalDateTime;

public record NotificationSearchQuery(
        AlertStatus status,
        AlertSeverity severity,
        AlertSource source,
        String job,
        LocalDateTime from,
        LocalDateTime to,
        String actionUserEmail,
        Boolean handledOnly,
        int page
) {
    public NotificationSearchCriteria toCriteria() {
        return new NotificationSearchCriteria(
                status, severity, source, job, from, to, actionUserEmail, handledOnly
        );
    }

    public PagingRequest toPagingRequest(int pageSize) {
        return new PagingRequest(page, pageSize);
    }
}