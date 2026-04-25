package io.antcamp.notificationservice.application.dto.command;

import io.antcamp.notificationservice.presentation.dto.request.PrometheusWebhookRequest;

import java.util.List;

public record PrometheusAlertCommand(
        String receiver,
        String status,
        List<AlertItem> alerts
) {
    public static PrometheusAlertCommand from(PrometheusWebhookRequest request) {
        List<AlertItem> items = request.alerts().stream()
                .map(alert -> new AlertItem(
                        alert.getLabel("alertname"),
                        alert.getLabel("severity"),
                        alert.getLabel("job"),
                        alert.getLabel("instance"),
                        alert.getLabel("uri"),
                        alert.getLabel("exception"),
                        alert.getLabel("status"),
                        alert.getAnnotation("summary"),
                        alert.getAnnotation("description"),
                        alert.fingerprint(),
                        alert.isFiring()
                ))
                .toList();

        return new PrometheusAlertCommand(request.receiver(), request.status(), items);
    }

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