package io.antcamp.competitionservice.application.dto;

import java.util.UUID;

public record CancelCompetitionCommand(
        UUID competitionId,
        UUID userId
) {
}
