package io.antcamp.notificationservice.infrastructure.client.monitoring;

final class PromQLQueries {

    private PromQLQueries() {}

    static String cpu(String job) {
        return String.format("process_cpu_usage{job=\"%s\"}", job);
    }

    static String heap(String job) {
        return String.format(
                "jvm_memory_used_bytes{job=\"%s\",area=\"heap\"} / jvm_memory_max_bytes{job=\"%s\",area=\"heap\"} > 0",
                job, job);
    }

    static String httpErrorRate(String job) {
        return String.format(
                "sum(rate(http_server_requests_seconds_count{job=\"%s\",status=~\"[45]..\"}[5m]))",
                job);
    }

    static String avgResponseTime(String job) {
        return String.format(
                "sum(rate(http_server_requests_seconds_sum{job=\"%s\"}[5m])) / sum(rate(http_server_requests_seconds_count{job=\"%s\"}[5m]))",
                job, job);
    }
}