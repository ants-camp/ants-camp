package io.antcamp.rankingservice.application.dto;

import java.util.UUID;

public record RankingResult(
        UUID userId,
        Double totalAsset,
        long rank
) {
}
