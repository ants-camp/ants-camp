package io.antcamp.notificationservice.application.service;

public final class PromQLQueries {

    private PromQLQueries() {}

    public static String cpu(String job) {
        return String.format("process_cpu_usage{job=\"%s\"}", job);
    }

    public static String heap(String job) {
        return String.format(
                "jvm_memory_used_bytes{job=\"%s\",area=\"heap\"} / jvm_memory_max_bytes{job=\"%s\",area=\"heap\"} > 0",
                job, job);
    }

    public static String httpErrorRate(String job) {
        return String.format(
                "sum(rate(http_server_requests_seconds_count{job=\"%s\",status=~\"[45]..\"}[5m]))",
                job);
    }

    public static String avgResponseTime(String job) {
        return String.format(
                "sum(rate(http_server_requests_seconds_sum{job=\"%s\"}[5m])) / sum(rate(http_server_requests_seconds_count{job=\"%s\"}[5m]))",
                job, job);
    }
}