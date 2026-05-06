package io.antcamp.rankingservice.presentation.dto;

import java.util.UUID;

public record FinalizeRankingsResponse(
        UUID competitionId,
        int finalizedCount
) {
}
