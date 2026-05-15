package io.antcamp.rankingservice.presentation.dto;

import io.antcamp.rankingservice.application.dto.CompetitionHistoryResult;
import io.antcamp.rankingservice.domain.model.RankTier;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * GET /api/rankings/me — 유저의 대회 참여 이력 응답 DTO
 */
public record MyCompetitionHistoryResponse(
        UUID competitionId,
        RankTier rankTier,
        LocalDateTime lastUpdatedAt
) {
    public static MyCompetitionHistoryResponse from(CompetitionHistoryResult result) {
        return new MyCompetitionHistoryResponse(
                result.competitionId(),
                result.rankTier(),
                result.lastUpdatedAt()
        );
    }
}
