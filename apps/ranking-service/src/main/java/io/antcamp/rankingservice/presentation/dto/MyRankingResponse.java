// presentation/dto/MyRankingResponse.java
package io.antcamp.rankingservice.presentation.dto;

import io.antcamp.rankingservice.application.dto.RankingResult;
import java.math.BigDecimal;
import java.util.UUID;

public record MyRankingResponse(UUID userId, long rank, BigDecimal totalAsset) {
    public static MyRankingResponse from(RankingResult result) {
        return new MyRankingResponse(result.userId(), result.rank(), result.totalAsset());
    }
}
