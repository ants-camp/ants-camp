package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.infrastructure.entity.JoinHistoryEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JoinHistoryJpaRepository extends JpaRepository<JoinHistoryEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM JoinHistoryEntity j WHERE j.userId = :userId AND j.competitionId = :competitionId")
    Optional<JoinHistoryEntity> findByUserIdAndCompetitionIdWithLock(
            @Param("userId") UUID userId,
            @Param("competitionId") UUID competitionId
    );
}
