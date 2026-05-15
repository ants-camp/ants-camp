package io.antcamp.tradeservice.domain.repository;

import io.antcamp.tradeservice.domain.model.Trade;

import java.util.List;
import java.util.UUID;

public interface TradeRepository {
    Trade findById(UUID tradeId);
    Trade save(Trade trade);
    void updateStatus(Trade trade);

    /** status=PENDING & orderType=LIMIT 인 미체결 지정가 주문 전체 조회 */
    List<Trade> findPendingLimitOrders();

    /** 특정 계좌의 미체결 주문 목록 조회 */
    List<Trade> findPendingOrdersByAccountId(UUID accountId);

    /**
     * 특정 계좌+종목의 미체결 지정가 매도 주문 수량 합계.
     * 새 매도 주문 접수 시 가용 수량(보유 - 잠금) 계산에 사용.
     */
    int sumPendingLimitSellQuantity(UUID accountId, String stockCode);
}
