package io.antcamp.tradeservice.presentation.dto;

import java.util.UUID;

/**
 * 매수/매도 주문 결과 DTO
 *
 * status
 *   EXECUTED — 체결 완료
 *   PENDING  — 지정가 조건 미충족, 미체결 대기
 *   CANCELLED — 주문 취소
 */
public record TradeOrderResponse(
        String stockCode,
        String stockName,
        String orderType,       // "MARKET" | "LIMIT"
        String side,            // "BUY" | "SELL"
        String status,          // "EXECUTED" | "PENDING" | "CANCELLED"
        double executedPrice,   // 주당 체결가 (미체결 시 현재가)
        double totalAmount,     // 체결 총액 (미체결/취소 시 0)
        int    stockAmount,
        UUID   tradeId,         // 주문 ID (미체결 주문 취소 시 사용)
        String message
) {
    /** 체결 완료 */
    public static TradeOrderResponse executed(
            String stockCode, String stockName,
            String orderType, String side,
            double price, int amount) {
        return new TradeOrderResponse(
                stockCode, stockName, orderType, side,
                "EXECUTED", price, price * amount, amount, null,
                String.format("%s %d주 체결 완료 (체결가: %,.0f원)",
                        side.equalsIgnoreCase("BUY") ? "매수" : "매도", amount, price)
        );
    }

    /** 지정가 미체결 — PENDING 저장됨 */
    public static TradeOrderResponse pending(
            String stockCode, String stockName,
            String orderType, String side,
            double currentPrice, double limitPrice, int amount, UUID tradeId) {
        String cond = side.equalsIgnoreCase("BUY")
                ? String.format("현재가(%.0f) > 지정가(%.0f)", currentPrice, limitPrice)
                : String.format("현재가(%.0f) < 지정가(%.0f)", currentPrice, limitPrice);
        return new TradeOrderResponse(
                stockCode, stockName, orderType, side,
                "PENDING", currentPrice, 0, amount, tradeId,
                "지정가 미체결 접수 — " + cond + " | tradeId=" + tradeId
        );
    }

    /** 주문 취소 완료 */
    public static TradeOrderResponse cancelled(
            String stockCode, String side,
            Double limitPrice, int amount, UUID tradeId) {
        return new TradeOrderResponse(
                stockCode, null, "LIMIT", side,
                "CANCELLED", limitPrice != null ? limitPrice : 0, 0, amount, tradeId,
                String.format("주문 취소 완료 — tradeId=%s", tradeId)
        );
    }
}
