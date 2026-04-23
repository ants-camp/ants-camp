package io.antcamp.competitionservice.presentation.dto;

import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.domain.CompetitionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindCompetitionListResponse(
        UUID competitionId,
        String name,
        CompetitionType type,
        CompetitionStatus status,
        String description,
        int firstSeed,
        LocalDateTime registerStartAt,
        LocalDateTime registerEndAt,
        LocalDateTime competitionStartAt,
        LocalDateTime competitionEndAt,
        int minParticipants,
        int maxParticipants,
        int currentRegisters
) {
    public static FindCompetitionListResponse from(Competition competition) {
        return new FindCompetitionListResponse(
                competition.getCompetitionId(),
                competition.getName(),
                competition.getType(),
                competition.getStatus(),
                competition.getDescription(),
                competition.getFirstSeed(),
                competition.getRegisterPeriod().getStartAt(),
                competition.getRegisterPeriod().getEndAt(),
                competition.getCompetitionPeriod().getStartAt(),
                competition.getCompetitionPeriod().getEndAt(),
                competition.getParticipantCount().getMin(),
                competition.getParticipantCount().getMax(),
                competition.getParticipantCount().getCurrent()
        );
    }
}
