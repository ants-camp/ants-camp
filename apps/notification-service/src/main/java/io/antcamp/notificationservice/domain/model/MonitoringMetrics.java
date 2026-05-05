package io.antcamp.notificationservice.domain.model;

public record MonitoringMetrics(
        String job,                         // 서비스
        Double cpuUsage,                    // cpu 사용률
        Double heapUsageRatio,              // heap 사용률
        Double httpErrorCount,              // 5분간 에러 건 수
        Double avgResponseTimeSeconds       // 평균 응답 시간 (초)
) {
    public MonitoringMetrics {
        if (cpuUsage != null && (cpuUsage < 0 || cpuUsage > 1))
            throw new IllegalArgumentException("cpuUsage must be in [0,1]");
        if (heapUsageRatio != null && (heapUsageRatio < 0 || heapUsageRatio > 1))
            throw new IllegalArgumentException("heapUsageRatio must be in [0,1]");
    }
    public static MonitoringMetrics empty(String job) {
        return new MonitoringMetrics(job, null, null, null, null);
    }

    public boolean isValid() {
        return cpuUsage != null || heapUsageRatio != null
                || httpErrorCount != null || avgResponseTimeSeconds != null;
    }

    public String formatCpu() {
        return cpuUsage != null ? String.format("%.1f%%", cpuUsage * 100) : "N/A";
    }

    public String formatHeap() {
        return heapUsageRatio != null ? String.format("%.1f%%", heapUsageRatio * 100) : "N/A";
    }

    public String formatErrorCount() {
        return httpErrorCount != null ? String.format("%.0f건", httpErrorCount) : "N/A";
    }

    public String formatAvgResponseTime() {
        return avgResponseTimeSeconds != null ? String.format("%.0fms", avgResponseTimeSeconds * 1000) : "N/A";
    }
}