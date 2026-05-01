package io.antcamp.assetservice.domain.event.payload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CompetitionEndedEvent(
        UUID competitionId,
        List<UUID> participantUserIds,
        LocalDateTime endedAt
) {
}