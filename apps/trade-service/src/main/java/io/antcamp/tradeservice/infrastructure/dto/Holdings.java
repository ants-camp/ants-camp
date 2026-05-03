package io.antcamp.tradeservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Holdings(
        @JsonProperty("stock-name")
        String stockName,
        @JsonProperty("stock-code")
        String stockCode,
        @JsonProperty("stock-amount")
        int stockAmount
) {
}
