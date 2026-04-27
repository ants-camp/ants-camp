package io.antcamp.tradeservice.infrastructure.dto;

import java.util.List;

/**
 * 실시간 호가 데이터 (H0STASP0)
 *
 * asks: 매도호가 리스트 (index 0 = 가장 낮은 매도호가 = 1호가)
 * bids: 매수호가 리스트 (index 0 = 가장 높은 매수호가 = 1호가)
 */
public record OrderBookData(
        String stockCode,
        String tradeTime,
        List<OrderLevel> asks,
        List<OrderLevel> bids,
        long totalAskQty,
        long totalBidQty
) {
    public record OrderLevel(long price, long qty) {}
}
