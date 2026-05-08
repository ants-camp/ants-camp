package io.antcamp.tradeservice.domain.repository;

import io.antcamp.tradeservice.domain.model.Trade;

import java.util.List;
import java.util.UUID;

public interface TradeRepository {
    Trade findById(UUID tradeId);
    void save(Trade trade);
    void updateStatus(Trade trade);

    /** status=PENDING & orderType=LIMIT 인 미체결 지정가 주문 전체 조회 */
    List<Trade> findPendingLimitOrders();

    /** 특정 계좌의 미체결 주문 목록 조회 */
    List<Trade> findPendingOrdersByAccountId(UUID accountId);
}
