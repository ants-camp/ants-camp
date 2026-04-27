package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import java.util.List;
import java.util.UUID;

public interface CompetitionChangeNoticeRepository {
    CompetitionChangeNotice save(CompetitionChangeNotice notice);

    List<CompetitionChangeNotice> findAllByCompetitionId(UUID competitionId);
}
