package io.antcamp.assetservice.infrastructure.messaging.kafka.payload;

import java.util.UUID;

public record CompetitionRegisteredPayload(
        UUID competitionId,
        String competitionName,
        String competitionType,
        int firstSeed,
        UUID userId
) {
}