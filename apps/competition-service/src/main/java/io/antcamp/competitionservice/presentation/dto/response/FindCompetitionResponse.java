package io.antcamp.competitionservice.presentation.dto.response;

import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.domain.model.CompetitionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindCompetitionResponse(
        UUID competitionId,
        String name,
        CompetitionType type,
        CompetitionStatus status,
        String description,
        int firstSeed,
        boolean isReadable,
        LocalDateTime registerStartAt,
        LocalDateTime registerEndAt,
        LocalDateTime competitionStartAt,
        LocalDateTime competitionEndAt,
        int minParticipants,
        int maxParticipants,
        int currentRegisters
) {
    public static FindCompetitionResponse from(Competition competition) {
        return new FindCompetitionResponse(
                competition.getCompetitionId(),
                competition.getName(),
                competition.getType(),
                competition.getStatus(),
                competition.getDescription(),
                competition.getFirstSeed(),
                competition.isReadable(),
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
