package io.antcamp.tradeservice.presentation.dto;

import java.util.UUID;

/**
 * asset-service /api/holdings/sell 요청 DTO
 * SellHoldingCommand 와 동일한 구조 (필드명·타입 일치 필수)
 */
public record AssetSellRequest(
        UUID accountId,
        String stockCode,
        Integer stockAmount,
        Long price                // 주당 매도 가격
) {
}
