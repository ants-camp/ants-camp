package io.antcamp.tradeservice.presentation.dto;

public record SellStockResponse(
        String stockCode,
        String stockName,
        double stockPrice,
        int stockAmount
) {
}
