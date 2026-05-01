package io.antcamp.assetservice.domain.repository;

import io.antcamp.assetservice.application.dto.query.ParticipantTotalAssetResult;

import java.util.List;
import java.util.UUID;

public interface TotalAssetEventProducer {
    void sendTotalAssetCalculated(UUID competitionId, List<ParticipantTotalAssetResult> totalAssets);
}