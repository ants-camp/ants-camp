package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;

public interface JoinHistoryService {
    void join(JoinCompetitionCommand command);

    void cancel(JoinCompetitionCommand command);
}
