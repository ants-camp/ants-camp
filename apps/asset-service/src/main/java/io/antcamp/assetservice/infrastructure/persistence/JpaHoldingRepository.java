package io.antcamp.assetservice.infrastructure.persistence;

import io.antcamp.assetservice.infrastructure.entity.HoldingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaHoldingRepository extends JpaRepository<HoldingEntity, UUID> {

    Optional<HoldingEntity> findByAccountIdAndStockCode(UUID accountId, String stockCode);

    List<HoldingEntity> findAllByAccountId(UUID accountId);
}