package io.antcamp.rankingservice.domain.repository;

import java.util.List;
import java.util.UUID;

/**
 * 대회 진행 중 랭킹 갱신/조회 기능 제공 (Redis)
 */
public interface RankingRedisRepository {

    // Update
    /** 해당 유저의 총자산 점수를 Redis ZSet에 저장/갱신 (대회 종료 후 최종 동기화 시 사용) */
    void upsertScore(UUID competitionId, UUID userId, Double totalAsset);

    // Read
    long getRank(UUID competitionId, UUID userId);       // 0-based 순위
    Double getScore(UUID competitionId, UUID userId);    // 총자산 점수 (없으면 null)
    long getTotalCount(UUID competitionId);              // 해당 대회 참가자 수

    // Search
    /** 총자산 높은 순으로 페이징 조회 */
    List<RankingEntry> getTopRankings(UUID competitionId, long offset, long count);

    record RankingEntry(UUID userId, Double totalAsset, long rank) {
    }
}
