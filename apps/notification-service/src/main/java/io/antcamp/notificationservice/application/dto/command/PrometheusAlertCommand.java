package io.antcamp.notificationservice.application.dto.command;

import io.antcamp.notificationservice.domain.exception.InvalidInputException;

import java.util.List;

public record PrometheusAlertCommand(
        String receiver,
        String status,
        List<AlertItem> alerts
) {

    public record AlertItem(
            String alertName,
            String severity,
            String job,
            String instance,
            String uri,
            String exception,
            String httpStatus,
            String summary,
            String description,
            String fingerprint,
            boolean firing,
            String startsAt
    ) {
        public AlertItem {
            if (alertName == null || alertName.isBlank()) throw new InvalidInputException();
            if (fingerprint == null || fingerprint.isBlank()) throw new InvalidInputException();
            if (job == null || job.isBlank()) throw new InvalidInputException();
        }
    }
}