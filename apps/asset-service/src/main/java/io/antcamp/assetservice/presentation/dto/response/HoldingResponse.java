package io.antcamp.assetservice.presentation.dto.response;

import io.antcamp.assetservice.application.dto.query.HoldingResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class HoldingResponse {

    private UUID holdingId;
    private UUID accountId;
    private String stockCode;
    private Integer stockAmount;
    private Long buyPrice;
    private Long finalPrice;

    public static HoldingResponse from(HoldingResult result) {
        return new HoldingResponse(
                result.getHoldingId(),
                result.getAccountId(),
                result.getStockCode(),
                result.getStockAmount(),
                result.getBuyPrice(),
                result.getFinalPrice()
        );
    }
}