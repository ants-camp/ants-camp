package io.antcamp.tradeservice.infrastructure.event.producer;

import java.util.UUID;

public record TradeSucceededEvent(
        double totalAsset,
        UUID accountId,
        UUID tradeId
) {
}
