package io.antcamp.assetservice.application.dto.query;

import java.util.UUID;

public record ParticipantTotalAssetResult(
        UUID userId,
        long totalAsset
) {
}