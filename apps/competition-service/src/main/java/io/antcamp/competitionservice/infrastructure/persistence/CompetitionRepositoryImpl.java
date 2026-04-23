package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompetitionRepositoryImpl implements CompetitionRepository {

    private final CompetitionJpaRepository competitionJpaRepository;

    @Override
    public Competition save(Competition competition) {
        CompetitionEntity entity = CompetitionEntity.from(competition);
        CompetitionEntity saved = competitionJpaRepository.save(entity);
        return saved.toDomain();
    }
}
