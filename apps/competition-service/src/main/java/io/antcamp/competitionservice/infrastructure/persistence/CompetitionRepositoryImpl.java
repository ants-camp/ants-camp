package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    @Override
    public Optional<Competition> findById(UUID id) {
        return competitionJpaRepository.findById(id)
                .map(CompetitionEntity::toDomain);
    }

    @Override
    public List<Competition> findAll() {
        return competitionJpaRepository.findAll()
                .stream()
                .map(CompetitionEntity::toDomain)
                .toList();
    }

    @Override
    public List<Competition> findAllByCompetitionStatus(CompetitionStatus status) {
        return competitionJpaRepository.findAllByStatus(status)
                .stream()
                .map(CompetitionEntity::toDomain)
                .toList();
    }
}
