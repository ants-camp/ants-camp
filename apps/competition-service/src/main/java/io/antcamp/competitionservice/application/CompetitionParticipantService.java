package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.CancelCompetitionCommand;
import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import java.util.List;
import java.util.UUID;

public interface CompetitionParticipantService {

    // Create
    CompetitionParticipant registerCompetition(JoinCompetitionCommand command);

    // Delete
    CompetitionParticipant cancelRegistration(CancelCompetitionCommand command);

    // Search
    List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId);
}
