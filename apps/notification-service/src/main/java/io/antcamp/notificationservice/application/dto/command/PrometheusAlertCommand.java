package io.antcamp.notificationservice.application.dto.command;

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
            boolean firing
    ) {}
}