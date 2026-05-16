package io.antcamp.rankingservice.domain.repository;

import io.antcamp.rankingservice.domain.model.Ranking;
import java.util.List;
import java.util.UUID;

/**
 * 대회 종료 후 랭킹 저장/조회 기능 제공
 */
public interface RankingRepository {

    // Create / Update
    Ranking save(Ranking ranking);

    // Search
    /** 특정 유저의 전체 대회 참여 이력 (확정된 것만) */
    List<Ranking> findAllByUserId(UUID userId);
}
