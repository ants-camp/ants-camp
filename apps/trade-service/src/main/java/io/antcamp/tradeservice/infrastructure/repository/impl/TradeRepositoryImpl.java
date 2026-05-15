package io.antcamp.tradeservice.infrastructure.repository.impl;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.tradeservice.domain.model.OrderType;
import io.antcamp.tradeservice.domain.model.Trade;
import io.antcamp.tradeservice.domain.model.TradeStatus;
import io.antcamp.tradeservice.domain.repository.TradeRepository;
import io.antcamp.tradeservice.infrastructure.entity.TradeEntity;
import io.antcamp.tradeservice.infrastructure.repository.TradeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepository {

    private final TradeJpaRepository tradeJpaRepository;

    @Override
    public Trade findById(UUID tradeId) {
        return tradeJpaRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND))
                .toDomain();
    }

    @Override
    public Trade save(Trade trade) {
        return tradeJpaRepository.save(TradeEntity.fromDomain(trade)).toDomain();
    }

    @Override
    public void updateStatus(Trade trade) {
        tradeJpaRepository.findById(trade.tradeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND))
                .updateStatus(trade);
    }

    @Override
    public List<Trade> findPendingLimitOrders() {
        return tradeJpaRepository
                .findByTradeStatusAndOrderType(TradeStatus.PENDING, OrderType.LIMIT)
                .stream()
                .map(TradeEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Trade> findPendingOrdersByAccountId(UUID accountId) {
        return tradeJpaRepository
                .findByAccountIdAndTradeStatus(accountId, TradeStatus.PENDING)
                .stream()
                .map(TradeEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int sumPendingLimitSellQuantity(UUID accountId, String stockCode) {
        return tradeJpaRepository.sumPendingLimitSellQuantity(accountId, stockCode);
    }
}
