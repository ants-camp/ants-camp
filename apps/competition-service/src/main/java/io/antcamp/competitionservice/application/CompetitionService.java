package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompetitionService {

    // Create
    Competition create(CreateCompetitionCommand command);

    // Read
    Competition findById(UUID id);

    // Update
    Competition openCompetition(UUID competitionId);
    Competition updateInfo(UpdateCompetitionCommand command);
    Competition startCompetition(UUID competitionId);
    Competition finishCompetition(UUID competitionId);
    Competition cancelCompetition(UUID competitionId);

    // Delete
    void deleteCompetition(UUID competitionId, String deletedBy);

    // Search
    Page<Competition> findAll(Pageable pageable);
    Page<Competition> findAllByStatus(CompetitionStatus status, Pageable pageable);
    List<CompetitionChangeNotice> findChangeNotices(UUID competitionId);
}
