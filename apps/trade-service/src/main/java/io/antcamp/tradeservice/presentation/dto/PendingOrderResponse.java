package io.antcamp.tradeservice.presentation.dto;

import io.antcamp.tradeservice.domain.model.Trade;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 미체결 주문 조회 응답 DTO
 */
public record PendingOrderResponse(
        UUID   tradeId,
        String stockCode,
        String tradeType,   // "BUY" | "SELL"
        String orderType,   // "MARKET" | "LIMIT"
        Double limitPrice,
        int    stockAmount,
        double totalPrice,
        LocalDateTime tradeAt
) {
    public static PendingOrderResponse from(Trade trade) {
        return new PendingOrderResponse(
                trade.tradeId(),
                trade.stockCode(),
                trade.tradeType().name(),
                trade.orderType().name(),
                trade.limitPrice(),
                trade.stockAmount(),
                trade.totalPrice(),
                trade.tradeAt()
        );
    }
}
