package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetitionParticipantRepository {
    CompetitionParticipant save(CompetitionParticipant participant);

    Optional<CompetitionParticipant> findByUserIdAndCompetitionId(UUID userId, UUID competitionId);

    void delete(CompetitionParticipant participant, String deletedBy);

    List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId);
}
