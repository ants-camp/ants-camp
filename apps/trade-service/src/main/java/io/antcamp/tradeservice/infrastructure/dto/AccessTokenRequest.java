package io.antcamp.tradeservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenRequest(
        @JsonProperty("grant_type")
        String grantType,
        @JsonProperty("appkey")
        String appKey,
        @JsonProperty("appsecret")
        String appSecret
) {
}
