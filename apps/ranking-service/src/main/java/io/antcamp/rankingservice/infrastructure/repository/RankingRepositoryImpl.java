package io.antcamp.rankingservice.infrastructure.repository;

import io.antcamp.rankingservice.domain.model.Ranking;
import io.antcamp.rankingservice.domain.repository.RankingRepository;
import io.antcamp.rankingservice.infrastructure.entity.RankingEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final RankingJpaRepository jpaRepository;

    @Override
    public Ranking save(Ranking ranking) {
        return jpaRepository.save(RankingEntity.from(ranking)).toDomain();
    }

    @Override
    public Optional<Ranking> findByCompetitionIdAndUserId(UUID competitionId, UUID userId) {
        return jpaRepository.findByCompetitionIdAndUserId(competitionId, userId)
                .map(RankingEntity::toDomain);
    }

    @Override
    public List<Ranking> findAllByCompetitionId(UUID competitionId) {
        return jpaRepository.findAllByCompetitionId(competitionId)
                .stream()
                .map(RankingEntity::toDomain)
                .toList();
    }
}
