package io.antcamp.assetservice.application.dto.query;

import java.time.LocalDateTime;
import java.util.UUID;

public record TradeResult(
        UUID userId,
        String tradeType,
        LocalDateTime tradeAt,
        String stockCode,
        Integer stockAmount,
        Long stockPrice
) {
}