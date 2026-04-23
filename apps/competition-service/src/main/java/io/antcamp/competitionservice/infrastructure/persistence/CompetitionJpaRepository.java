package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionJpaRepository extends JpaRepository<CompetitionEntity, UUID> {
    List<CompetitionEntity> findAllByStatus(CompetitionStatus status);
}
