package io.antcamp.notificationservice.application.port;

import io.antcamp.notificationservice.domain.model.MonitoringMetrics;

public interface MonitoringPort {
    MonitoringMetrics collectMetrics(String job);
}