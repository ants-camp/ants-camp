package io.antcamp.competitionservice.application.dto;

import io.antcamp.competitionservice.domain.CompetitionType;
import java.time.LocalDateTime;

public record CreateCompetitionCommand(
        String name,
        CompetitionType type,
        String description,
        int firstSeed,
        LocalDateTime registerStartAt,
        LocalDateTime registerEndAt,
        LocalDateTime competitionStartAt,
        LocalDateTime competitionEndAt,
        int minParticipants,
        int maxParticipants
) {
}
