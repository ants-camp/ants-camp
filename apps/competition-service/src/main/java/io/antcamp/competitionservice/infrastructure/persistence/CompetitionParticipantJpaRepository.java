package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.infrastructure.entity.CompetitionParticipantEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionParticipantJpaRepository extends JpaRepository<CompetitionParticipantEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CompetitionParticipantEntity p WHERE p.userId = :userId AND p.competitionId = :competitionId")
    Optional<CompetitionParticipantEntity> findByUserIdAndCompetitionIdWithLock(
            @Param("userId") UUID userId,
            @Param("competitionId") UUID competitionId
    );

    List<CompetitionParticipantEntity> findAllByCompetitionId(UUID competitionId);
}
