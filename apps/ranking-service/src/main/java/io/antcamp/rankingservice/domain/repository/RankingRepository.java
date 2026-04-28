package io.antcamp.rankingservice.domain.repository;

import io.antcamp.rankingservice.domain.model.Ranking;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 대회 종료 후 랭킹 저장/조회 기능 제공
 */
public interface RankingRepository {
    // 대회 종료 후 최종 순위 기록용
    Ranking save(Ranking ranking);

    // 대회 종료후 특정 사용자의 대회 순위 조회
    Optional<Ranking> findByCompetitionIdAndUserId(UUID competitionId, UUID userId);

    // 대회 종료후 특정 대회의 랭킹 목록 조회
    List<Ranking> findAllByCompetitionId(UUID competitionId);
}
