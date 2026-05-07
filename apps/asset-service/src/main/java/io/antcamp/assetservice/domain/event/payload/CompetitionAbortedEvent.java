package io.antcamp.assetservice.domain.event.payload;

import java.util.List;
import java.util.UUID;

public record CompetitionAbortedEvent(
        UUID competitionId,
        List<UUID> participantUserIds
) {}