package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.infrastructure.entity.CompetitionChangeNoticeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionChangeNoticeJpaRepository extends JpaRepository<CompetitionChangeNoticeEntity, UUID> {
    List<CompetitionChangeNoticeEntity> findAllByCompetitionId(UUID competitionId);
}
