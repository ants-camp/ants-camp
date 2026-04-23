package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionEntity;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<Competition> findAll(Pageable pageable) {
        return competitionJpaRepository.findAll(pageable)
                .map(CompetitionEntity::toDomain);
    }

    @Override
    public Page<Competition> findAllByCompetitionStatus(CompetitionStatus status, Pageable pageable) {
        return competitionJpaRepository.findAllByStatus(status, pageable)
                .map(CompetitionEntity::toDomain);
    }
}
