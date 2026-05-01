package io.antcamp.assetservice.infrastructure.messaging.kafka.payload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CompetitionEndedEvent(
        UUID competitionId,
        List<UUID> participantUserIds,
        LocalDateTime endedAt
) {
}