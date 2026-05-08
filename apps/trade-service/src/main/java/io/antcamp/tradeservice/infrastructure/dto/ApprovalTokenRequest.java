package io.antcamp.tradeservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApprovalTokenRequest(
        @JsonProperty("grant_type")
        String grantType,
        @JsonProperty("appkey")
        String appKey,
        @JsonProperty("secretkey")
        String appSecret
) {
}
