package io.antcamp.competitionservice.presentation.dto;

import io.antcamp.competitionservice.domain.CompetitionType;
import java.time.LocalDateTime;

public record CreateCompetitionRequest(
        String name,
        CompetitionType type,
        String description,
        LocalDateTime registerStartAt,
        LocalDateTime registerEndAt,
        LocalDateTime competitionStartAt,
        LocalDateTime competitionEndAt,
        int minParticipants,
        int maxParticipants
) {
}
