package io.antcamp.notificationservice.application.port;

import io.antcamp.notificationservice.application.dto.command.PrometheusAlertCommand;
import io.antcamp.notificationservice.domain.model.MonitoringMetrics;

public interface LlmPort {
    String analyze(PrometheusAlertCommand.AlertItem alert, MonitoringMetrics metrics, String recentLogs);
}