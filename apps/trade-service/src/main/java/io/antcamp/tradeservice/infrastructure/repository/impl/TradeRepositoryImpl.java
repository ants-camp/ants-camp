package io.antcamp.tradeservice.infrastructure.repository.impl;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.tradeservice.domain.model.Trade;
import io.antcamp.tradeservice.domain.repository.TradeRepository;
import io.antcamp.tradeservice.infrastructure.entity.TradeEntity;
import io.antcamp.tradeservice.infrastructure.repository.TradeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepository {

    private final TradeJpaRepository tradeJpaRepository;

    @Override
    public Trade findById(UUID tradeId) {
        return tradeJpaRepository.findById(tradeId).orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND)
        ).toDomain();
    }

    @Override
    public void save(Trade trade) {
        tradeJpaRepository.save(TradeEntity.fromDomain(trade));
    }

}
