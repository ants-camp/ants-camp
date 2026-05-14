package io.antcamp.assetservice.domain.repository;

import io.antcamp.assetservice.domain.model.Holding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldingRepository {

    Holding save(Holding holding);

    Optional<Holding> findByAccountIdAndStockCode(UUID accountId, String stockCode);

    List<Holding> findAllByAccountId(UUID accountId);

    Optional<Holding> findByAccountIdAndStockCodeWithLock(UUID accountId, String stockCode);

    void delete(Holding holding);
}