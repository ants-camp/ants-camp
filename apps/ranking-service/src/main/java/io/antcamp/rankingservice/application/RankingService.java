package io.antcamp.rankingservice.application;

import io.antcamp.rankingservice.application.dto.RankingResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RankingService {
    // 매매 체결 시 Redis 갱신
    void upsertRanking(UUID competitionId, UUID userId, BigDecimal totalAsset);

    // 대회 전체 랭킹 조회 (진행 중)
    List<RankingResult> getTopRankings(UUID competitionId, int page, int size);

    // 내 순위 조회 (진행 중)
    RankingResult getMyRanking(UUID competitionId, UUID userId);

    // 대회 종료 시 최종 순위 확정
    void finalizeRankings(UUID competitionId);
}
