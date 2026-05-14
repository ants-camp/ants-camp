package io.antcamp.tradeservice.presentation.dto;

public record BuyStockRequest(
        String stockCode,
        int stockAmount
) {
}
