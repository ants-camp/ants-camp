package io.antcamp.assetservice.infrastructure.messaging.kafka.payload;

import java.util.List;
import java.util.UUID;

public record TotalAssetCalculatedEvent(
        UUID competitionId,
        List<ParticipantTotalAsset> totalAssets
) {
    public record ParticipantTotalAsset(
            UUID userId,
            long totalAsset
    ) {}
}