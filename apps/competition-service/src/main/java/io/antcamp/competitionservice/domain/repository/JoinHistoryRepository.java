package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.model.JoinHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JoinHistoryRepository {
    JoinHistory save(JoinHistory joinHistory);

    Optional<JoinHistory> findByUserIdAndCompetitionId(UUID userId, UUID competitionId);

    void delete(JoinHistory joinHistory, String deletedBy);

    List<JoinHistory> findAllByCompetitionId(UUID competitionId);
}
