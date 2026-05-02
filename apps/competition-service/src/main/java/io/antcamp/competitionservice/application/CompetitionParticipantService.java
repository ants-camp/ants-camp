package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import java.util.List;
import java.util.UUID;

public interface CompetitionParticipantService {

    // Create
    void registerCompetition(JoinCompetitionCommand command);

    // Delete
    void cancelRegistration(JoinCompetitionCommand command);

    // Search
    List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId);
}
