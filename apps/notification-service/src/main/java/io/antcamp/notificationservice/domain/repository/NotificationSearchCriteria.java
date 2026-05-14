package io.antcamp.notificationservice.domain.repository;

import io.antcamp.notificationservice.domain.model.AlertSeverity;
import io.antcamp.notificationservice.domain.model.AlertSource;
import io.antcamp.notificationservice.domain.model.AlertStatus;

import java.time.LocalDateTime;

public record NotificationSearchCriteria(
        AlertStatus status,
        AlertSeverity severity,
        AlertSource source,
        String job,
        LocalDateTime from,
        LocalDateTime to,
        String actionUserEmail,
        Boolean handledOnly
) {}
