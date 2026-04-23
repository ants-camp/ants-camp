package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.infrastructure.entity.CompetitionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionJpaRepository extends JpaRepository<CompetitionEntity, UUID> {
}
