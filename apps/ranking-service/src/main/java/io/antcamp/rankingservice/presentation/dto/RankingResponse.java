package io.antcamp.rankingservice.presentation.dto;

import io.antcamp.rankingservice.application.dto.RankingResult;
import java.util.UUID;

public record RankingResponse(long rank, UUID userId, Double totalAsset) {
    public static RankingResponse from(RankingResult result) {
        return new RankingResponse(result.rank(), result.userId(), result.totalAsset());
    }
}
