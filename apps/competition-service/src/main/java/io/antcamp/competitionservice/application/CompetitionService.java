package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompetitionService {
    Competition create(CreateCompetitionCommand command);

    Competition findById(UUID id);

    Page<Competition> findAll(Pageable pageable);

    Page<Competition> findAllByStatus(CompetitionStatus status, Pageable pageable);
}
