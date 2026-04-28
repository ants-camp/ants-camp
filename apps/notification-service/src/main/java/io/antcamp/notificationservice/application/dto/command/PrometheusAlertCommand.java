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
            boolean firing,
            String startsAt
    ) {
        public AlertItem {
            if (alertName == null || alertName.isBlank()) throw new IllegalArgumentException("alertName은 필수입니다.");
            if (fingerprint == null || fingerprint.isBlank()) throw new IllegalArgumentException("fingerprint는 필수입니다.");
            if (job == null || job.isBlank()) throw new IllegalArgumentException("job은 필수입니다.");
        }
    }
}