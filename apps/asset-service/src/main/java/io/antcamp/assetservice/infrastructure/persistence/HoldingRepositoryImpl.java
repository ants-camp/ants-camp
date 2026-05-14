package io.antcamp.assetservice.infrastructure.persistence;

import io.antcamp.assetservice.domain.exception.HoldingNotFoundException;
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
        HoldingEntity entity = jpaHoldingRepository.findById(holding.getHoldingId())
                .orElseThrow(() -> new HoldingNotFoundException("보유 주식을 찾을 수 없습니다."));
        entity.softDelete("SYSTEM");
        jpaHoldingRepository.save(entity);
    }
}