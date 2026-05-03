package io.antcamp.assetservice.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AssetResult {

    private UUID accountId;
    private Long accountAmount;
    private Long holdingEvaluationAmount;
    private Long totalAssetAmount;
}