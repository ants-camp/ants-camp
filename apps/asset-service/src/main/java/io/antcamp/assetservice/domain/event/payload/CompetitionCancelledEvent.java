package io.antcamp.assetservice.domain.event.payload;

import java.util.UUID;

public record CompetitionCancelledEvent(
        UUID competitionId,
        UUID userId
) {}