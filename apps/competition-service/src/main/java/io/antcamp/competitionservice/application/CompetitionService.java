package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.domain.Competition;

public interface CompetitionService {
    Competition create(CreateCompetitionCommand command);
}
