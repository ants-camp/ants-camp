package io.antcamp.tradeservice.presentation.dto;

public record BuyStockResponse(
        String stockCode,
        String stockName,
        double stockPrice,
        int stockAmount
) {
}
