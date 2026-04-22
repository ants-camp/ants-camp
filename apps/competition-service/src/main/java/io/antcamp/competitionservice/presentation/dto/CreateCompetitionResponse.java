package io.antcamp.competitionservice.presentation.dto;

import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.domain.CompetitionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCompetitionResponse(
        UUID competitionId,
        String name,
        CompetitionType type,
        CompetitionStatus status,
        LocalDateTime registerStartAt,
        LocalDateTime registerEndAt,
        LocalDateTime competitionStartAt,
        LocalDateTime competitionEndAt,
        int minParticipants,
        int maxParticipants
) {
    public static CreateCompetitionResponse from(Competition competition) {
        return new CreateCompetitionResponse(
                competition.getCompetitionId(),
                competition.getName(),
                competition.getType(),
                competition.getStatus(),
                competition.getRegisterPeriod().getStartAt(),
                competition.getRegisterPeriod().getEndAt(),
                competition.getCompetitionPeriod().getStartAt(),
                competition.getCompetitionPeriod().getEndAt(),
                competition.getParticipantCount().getMin(),
                competition.getParticipantCount().getMax()
        );
    }
}
