package io.antcamp.rankingservice.infrastructure.repository;

import io.antcamp.rankingservice.infrastructure.entity.RankingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RankingJpaRepository extends JpaRepository<RankingEntity, UUID> {
    /** 특정 유저의 전체 대회 참여 이력 (확정된 것만) */
    List<RankingEntity> findAllByUserIdAndIsFinalizedTrue(UUID userId);
}
