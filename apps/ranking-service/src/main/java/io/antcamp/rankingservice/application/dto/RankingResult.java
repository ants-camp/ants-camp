// application/dto/RankingResult.java
package io.antcamp.rankingservice.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RankingResult(
        UUID userId,
        BigDecimal totalAsset,
        long rank
) {
}
