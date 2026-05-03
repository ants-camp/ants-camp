package io.antcamp.assetservice.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class HoldingResult {

    private UUID holdingId;
    private UUID accountId;
    private String stockCode;
    private Integer stockAmount;
    private Long buyPrice;
    private Long finalPrice;
}