package io.antcamp.tradeservice.infrastructure.repository;

import io.antcamp.tradeservice.domain.model.OrderType;
import io.antcamp.tradeservice.domain.model.TradeStatus;
import io.antcamp.tradeservice.domain.model.TradeType;
import io.antcamp.tradeservice.infrastructure.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeJpaRepository extends JpaRepository<TradeEntity, UUID> {

    /** status=PENDING & orderType=LIMIT — 스케줄러용 */
    List<TradeEntity> findByTradeStatusAndOrderType(TradeStatus tradeStatus, OrderType orderType);

    /** 특정 계좌의 PENDING 주문 전체 (시장가/지정가 포함) */
    List<TradeEntity> findByAccountIdAndTradeStatus(UUID accountId, TradeStatus tradeStatus);

    /**
     * 특정 계좌+종목의 미체결 지정가 매도 주문 수량 합계.
     * 새 매도 주문 접수 시 "실제 보유 - 이 값 >= 요청 수량" 검증에 사용.
     */
    @Query("SELECT COALESCE(SUM(t.stockAmount), 0) FROM TradeEntity t " +
           "WHERE t.accountId = :accountId AND t.stockCode = :stockCode " +
           "AND t.tradeStatus = 'PENDING' AND t.orderType = 'LIMIT' AND t.tradeType = 'SELL'")
    int sumPendingLimitSellQuantity(@Param("accountId") UUID accountId,
                                    @Param("stockCode") String stockCode);
}
