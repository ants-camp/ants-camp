package io.antcamp.tradeservice.presentation.dto;

import java.util.UUID;

/**
 * asset-service /api/holdings/buy 요청 DTO
 * BuyHoldingCommand 와 동일한 구조
 */
public record BuyHoldingRequest(
        UUID accountId,
        String stockCode,
        Integer stockAmount,
        Long buyPrice       // 주당 가격 (총액 아님)
) {}
