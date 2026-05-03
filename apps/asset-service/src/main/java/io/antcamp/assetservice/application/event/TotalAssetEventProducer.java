package io.antcamp.assetservice.application.event;

import io.antcamp.assetservice.application.dto.query.ParticipantTotalAssetResult;

import java.util.List;
import java.util.UUID;

public interface TotalAssetEventProducer {
    void sendTotalAssetCalculated(UUID competitionId, List<ParticipantTotalAssetResult> totalAssets);
}