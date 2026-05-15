package io.antcamp.rankingservice.application.dto;

import io.antcamp.rankingservice.domain.model.RankTier;
import io.antcamp.rankingservice.domain.model.Ranking;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 유저의 대회 참여 이력 단건 — 서비스 → 컨트롤러 전달용
 */
public record CompetitionHistoryResult(
        UUID competitionId,
        RankTier rankTier,
        LocalDateTime lastUpdatedAt
) {
    public static CompetitionHistoryResult from(Ranking ranking) {
        return new CompetitionHistoryResult(
                ranking.getCompetitionId(),
                ranking.getRank(),
                ranking.getLastUpdatedAt()
        );
    }
}
