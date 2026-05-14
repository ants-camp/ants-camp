package io.antcamp.tradeservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Trade(
        UUID tradeId,
        UUID accountId,
        UUID userId,                   // 수정 전: 없음. 스케줄러가 체결 시 asset-service 호출에 X-User-Id 헤더로 사용
        TradeType tradeType,
        LocalDateTime tradeAt,
        String stockCode,
        int stockAmount,
        TradeStatus tradeStatus,
        double totalPrice,
        int retryCount,
        OrderType orderType,
        Double limitPrice
) {

    /** 신규 주문 생성 (PENDING 상태) */
    public static Trade create(UUID tradeId, UUID accountId, UUID userId, TradeType tradeType,
                               LocalDateTime tradeAt, String stockCode, int stockAmount,
                               double totalPrice, OrderType orderType, Double limitPrice) {
        return new Trade(
                tradeId, accountId, userId, tradeType, tradeAt, stockCode, stockAmount,
                TradeStatus.PENDING, totalPrice, 0, orderType, limitPrice
        );
    }

    /**
     * DB에서 복원할 때 사용.
     * Trade.create()는 항상 PENDING을 세팅하므로 별도 팩토리가 필요.
     */
    public static Trade fromPersistence(UUID tradeId, UUID accountId, UUID userId, TradeType tradeType,
                                        LocalDateTime tradeAt, String stockCode, int stockAmount,
                                        TradeStatus tradeStatus, double totalPrice, int retryCount,
                                        OrderType orderType, Double limitPrice) {
        return new Trade(
                tradeId, accountId, userId, tradeType, tradeAt, stockCode, stockAmount,
                tradeStatus, totalPrice, retryCount, orderType, limitPrice
        );
    }

    public static Trade updateSuccess(Trade trade) {
        return new Trade(
                trade.tradeId, trade.accountId, trade.userId, trade.tradeType, trade.tradeAt,
                trade.stockCode, trade.stockAmount, TradeStatus.SUCCESS, trade.totalPrice,
                trade.retryCount + 1, trade.orderType, trade.limitPrice
        );
    }

    public static Trade updateFail(Trade trade) {
        return new Trade(
                trade.tradeId, trade.accountId, trade.userId, trade.tradeType, trade.tradeAt,
                trade.stockCode, trade.stockAmount, TradeStatus.FAIL, trade.totalPrice,
                trade.retryCount + 1, trade.orderType, trade.limitPrice
        );
    }

    public static Trade updateCancelled(Trade trade) {
        return new Trade(
                trade.tradeId, trade.accountId, trade.userId, trade.tradeType, trade.tradeAt,
                trade.stockCode, trade.stockAmount, TradeStatus.CANCELLED, trade.totalPrice,
                trade.retryCount, trade.orderType, trade.limitPrice
        );
    }

    /**
     * 지정가 체결 조건 판별.
     * 매수: 현재가 <= 지정가 (싸게 살 수 있을 때)
     * 매도: 현재가 >= 지정가 (비싸게 팔 수 있을 때)
     */
    public boolean isLimitConditionMet(double currentPrice) {
        if (this.orderType != OrderType.LIMIT || this.limitPrice == null) return false;
        return this.tradeType == TradeType.BUY
                ? currentPrice <= this.limitPrice
                : currentPrice >= this.limitPrice;
    }
}
