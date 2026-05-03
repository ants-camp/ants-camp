package io.antcamp.rankingservice.domain.repository;

import io.antcamp.rankingservice.domain.model.Ranking;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 대회 종료 후 랭킹 저장/조회 기능 제공
 */
public interface RankingRepository {

    // Create / Update
    Ranking save(Ranking ranking);

    // Read
    Optional<Ranking> findByCompetitionIdAndUserId(UUID competitionId, UUID userId);

    // Search
    List<Ranking> findAllByCompetitionId(UUID competitionId);
}
