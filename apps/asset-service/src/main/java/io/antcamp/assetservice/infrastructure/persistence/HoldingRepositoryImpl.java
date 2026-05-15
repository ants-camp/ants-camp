package io.antcamp.assetservice.infrastructure.persistence;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import io.antcamp.assetservice.infrastructure.entity.HoldingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HoldingRepositoryImpl implements HoldingRepository {

    private final JpaHoldingRepository jpaHoldingRepository;

    @Override
    public Holding save(Holding holding) {
        return jpaHoldingRepository.save(HoldingEntity.from(holding)).toDomain();
    }

    @Override
    public Optional<Holding> findByAccountIdAndStockCode(UUID accountId, String stockCode) {
        return jpaHoldingRepository.findByAccountIdAndStockCode(accountId, stockCode)
                .map(HoldingEntity::toDomain);
    }

    @Override
    public List<Holding> findAllByAccountId(UUID accountId) {
        return jpaHoldingRepository.findAllByAccountId(accountId)
                .stream()
                .map(HoldingEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Holding> findByAccountIdAndStockCodeWithLock(UUID accountId, String stockCode) {
        return jpaHoldingRepository.findByAccountIdAndStockCodeWithLock(accountId, stockCode)
                .map(HoldingEntity::toDomain);
    }

    @Override
    public void delete(Holding holding) {
        // 수정 전: softDelete("SYSTEM") + save  ← deleted_at NULL row 가 DB 에 잔존했고,
        // uk_holdings_account_stock 유니크 제약은 deleted_at 을 고려하지 않아
        // 같은 (account_id, stock_code) 로 재매수 시 PostgreSQL 23505 발생.
        // Holding 은 "현재 보유 상태" 이므로 hard delete 가 적절. 매매 audit 은 p_trade 에 존재.
        HoldingEntity entity = jpaHoldingRepository.findById(holding.getHoldingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOLDING_NOT_FOUND)); // ✅ 변경
        entity.softDelete("SYSTEM");
        jpaHoldingRepository.save(entity);
    }
}