package io.antcamp.notificationservice.application.port;

public interface LogPort {
    String collectRecentLogs(String job);
}