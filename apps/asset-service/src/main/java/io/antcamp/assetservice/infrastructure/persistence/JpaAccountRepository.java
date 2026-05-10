package io.antcamp.assetservice.infrastructure.persistence;

import io.antcamp.assetservice.infrastructure.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAccountRepository extends JpaRepository<AccountEntity, UUID> {
    //비관적 락으로 잔액유실 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.accountId = :accountId")
    Optional<AccountEntity> findByIdWithLock(@Param("accountId") UUID accountId);

    List<AccountEntity> findAllByCompetitionId(UUID competitionId);

    Optional<AccountEntity> findByUserIdAndCompetitionId(UUID userId, UUID competitionId);

    List<AccountEntity> findAllByUserId(UUID userId);
}
