package io.antcamp.tradeservice.presentation.dto;

import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public record AssetSellRequest(
        UUID accountId,
        String stockCode,
        int stockAmount,
        double stockPrice
) {
}
