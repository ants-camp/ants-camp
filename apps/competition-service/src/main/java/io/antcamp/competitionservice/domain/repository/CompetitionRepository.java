package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.Competition;

public interface CompetitionRepository {
    Competition save(Competition competition);
}
