package io.antcamp.notificationservice.domain.model;

public enum AlertSeverity {
    CRITICAL,
    WARNING,
    INFO;

    public static AlertSeverity from(String value) {
        if (value == null) return INFO;
        return switch (value.toUpperCase()) {
            case "CRITICAL" -> CRITICAL;
            case "WARNING" -> WARNING;
            default -> INFO;
        };
    }
}
