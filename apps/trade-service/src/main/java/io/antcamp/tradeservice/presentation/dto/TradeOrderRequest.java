package io.antcamp.tradeservice.presentation.dto;

import java.util.UUID;

/**
 * 매수/매도 주문 요청 DTO
 * <p>
 * orderType MARKET — 시장가: 현재 체결가로 즉시 체결 LIMIT  — 지정가: limitPrice 조건 충족 시 체결 매수: 현재가 <= limitPrice 이면 체결 매도: 현재가 >=
 * limitPrice 이면 체결
 * <p>
 * side BUY  — 매수 SELL — 매도
 */
public record TradeOrderRequest(
        String stockCode,
        int stockAmount,
        String orderType,   // "MARKET" | "LIMIT"
        String side,        // "BUY"    | "SELL"
        Double limitPrice,   // 지정가 주문 시 필수, 시장가는 null
        UUID accountId
) {
    public boolean isMarket() {
        return "MARKET".equalsIgnoreCase(orderType);
    }

    public boolean isLimit() {
        return "LIMIT".equalsIgnoreCase(orderType);
    }
}
