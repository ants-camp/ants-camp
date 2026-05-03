package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetitionParticipantRepository {

    // Create
    CompetitionParticipant save(CompetitionParticipant participant);

    // Read
    Optional<CompetitionParticipant> findByUserIdAndCompetitionId(UUID userId, UUID competitionId);

    // Delete
    void delete(CompetitionParticipant participant, String deletedBy);

    // Search
    List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId);
}
