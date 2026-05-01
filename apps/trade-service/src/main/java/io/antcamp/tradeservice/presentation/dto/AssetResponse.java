package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.antcamp.tradeservice.infrastructure.dto.Holdings;
import jakarta.persistence.JoinColumn;

import java.util.List;

public record AssetResponse(
        @JsonProperty("user-id")
        String userId,
        @JsonProperty("account")
        int account,
        @JsonProperty("holdings")
        List<Holdings> holdings,
        @JsonProperty("can-trade")
        boolean canTrade
) {
}
