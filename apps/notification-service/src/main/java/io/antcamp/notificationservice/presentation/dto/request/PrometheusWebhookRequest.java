package io.antcamp.notificationservice.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record PrometheusWebhookRequest(
        String receiver,            //수신 채널 (slack)
        String status,              //상태
        List<Alert> alerts          //알림 내용
) {
    public record Alert(
            String status,
            Map<String, String> labels,
            Map<String, String> annotations,
            String fingerprint,
            @JsonProperty("startsAt") String startsAt,
            @JsonProperty("endsAt") String endsAt
    ) {
        public boolean isFiring() {
            return "firing".equalsIgnoreCase(this.status);
        }

        public String getLabel(String key) {
            return labels != null ? labels.get(key) : null;
        }

        public String getAnnotation(String key) {
            return annotations != null ? annotations.get(key) : null;
        }
    }
}