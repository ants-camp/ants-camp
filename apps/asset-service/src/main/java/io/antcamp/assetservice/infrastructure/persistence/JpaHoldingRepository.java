package io.antcamp.assetservice.infrastructure.persistence;

import io.antcamp.assetservice.infrastructure.entity.HoldingEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaHoldingRepository extends JpaRepository<HoldingEntity, UUID> {

    Optional<HoldingEntity> findByAccountIdAndStockCode(UUID accountId, String stockCode);

    List<HoldingEntity> findAllByAccountId(UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT h
        FROM HoldingEntity h
        WHERE h.accountId = :accountId
          AND h.stockCode = :stockCode
    """)
    Optional<HoldingEntity> findByAccountIdAndStockCodeWithLock(
            @Param("accountId") UUID accountId,
            @Param("stockCode") String stockCode
    );
}