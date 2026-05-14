package io.antcamp.notificationservice.domain.model;

import java.util.Locale;

public enum AlertSeverity {
    CRITICAL,
    WARNING,
    INFO;

    public static AlertSeverity from(String value) {
        if (value == null) return INFO;
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> CRITICAL;
            case "WARNING" -> WARNING;
            default -> INFO;
        };
    }
}
