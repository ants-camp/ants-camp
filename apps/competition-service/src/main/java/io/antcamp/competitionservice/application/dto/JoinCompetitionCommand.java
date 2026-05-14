package io.antcamp.competitionservice.application.dto;

import java.util.UUID;

public record JoinCompetitionCommand(
        UUID competitionId,
        UUID userId,
        String username) {
}
