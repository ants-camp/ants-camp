package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisAccessToken(
        @JsonProperty("access_token")
        String token
) {
}
