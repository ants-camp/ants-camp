package io.antcamp.tradeservice.infrastructure.repository;

import io.antcamp.tradeservice.domain.model.OrderType;
import io.antcamp.tradeservice.domain.model.TradeStatus;
import io.antcamp.tradeservice.infrastructure.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeJpaRepository extends JpaRepository<TradeEntity, UUID> {

    /** status=PENDING & orderType=LIMIT — 스케줄러용 */
    List<TradeEntity> findByTradeStatusAndOrderType(TradeStatus tradeStatus, OrderType orderType);

    /** 특정 계좌의 PENDING 주문 전체 (시장가/지정가 포함) */
    List<TradeEntity> findByAccountIdAndTradeStatus(UUID accountId, TradeStatus tradeStatus);
}
