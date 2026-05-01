package io.antcamp.rankingservice.domain.repository;

import java.util.List;
import java.util.UUID;

/**
 * 대회 진행중 랭킹 갱신/조회 기능 제공
 */
public interface RankingRedisRepository {
    // 매매 체결시, 랭킹 갱신
    void upsertScore(UUID competitionId, UUID userId, Double totalAsset);

    // 랭킹 단건 조회
    long getRank(UUID competitionId, UUID userId);   // 0-based

    // 특정 유저의 총자산 점수 조회 (없으면 null)
    Double getScore(UUID competitionId, UUID userId);

    // 해당 대회의 참가자 수
    long getTotalCount(UUID competitionId);

    // 평가금이 높은 순서로 목록을 반환 ( 페이징 적용 )
    List<RankingEntry> getTopRankings(UUID competitionId, long offset, long count);

    // 랭킹 목록 저장용 record
    record RankingEntry(UUID userId, Double totalAsset, long rank) {
    }
}
