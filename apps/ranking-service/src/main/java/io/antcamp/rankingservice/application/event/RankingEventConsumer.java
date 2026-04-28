package io.antcamp.rankingservice.application.event;

import io.antcamp.rankingservice.domain.event.AssetUpdatedPayload;

public interface RankingEventConsumer {
    void handleAssetUpdated(AssetUpdatedPayload payload);
}
