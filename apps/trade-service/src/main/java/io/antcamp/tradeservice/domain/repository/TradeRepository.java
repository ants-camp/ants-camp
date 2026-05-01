package io.antcamp.tradeservice.domain.repository;

import io.antcamp.tradeservice.domain.model.Trade;

import java.util.Optional;
import java.util.UUID;

public interface TradeRepository {
    Trade findById(UUID tradeId);
    void save(Trade trade);
}