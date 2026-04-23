package io.antcamp.competitionservice.application.dto;

import io.antcamp.competitionservice.domain.CompetitionType;
import io.antcamp.competitionservice.presentation.dto.CreateCompetitionRequest;
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
    public static CreateCompetitionCommand from(CreateCompetitionRequest request) {
        return new CreateCompetitionCommand(
                request.name(),
                request.type(),
                request.description(),
                request.firstSeed(),
                request.registerStartAt(),
                request.registerEndAt(),
                request.competitionStartAt(),
                request.competitionEndAt(),
                request.minParticipants(),
                request.maxParticipants()
        );
    }
}
