package io.antcamp.assetservice.domain.repository;

import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.TotalAssetCalculatedEvent;

public interface TotalAssetEventProducer {
    void sendTotalAssetCalculated(TotalAssetCalculatedEvent event);
}