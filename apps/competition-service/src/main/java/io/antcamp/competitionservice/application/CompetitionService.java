package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import java.util.List;
import java.util.UUID;

public interface CompetitionService {
    Competition create(CreateCompetitionCommand command);

    Competition findById(UUID id);

    List<Competition> findAll();

    List<Competition> findAllByStatus(CompetitionStatus status);
}
