package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
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
        return competitionJpaRepository.findById(competition.getCompetitionId())
                .map(entity -> {
                    // 기존 엔티티가 있으면 필드 업데이트
                    entity.update(competition);
                    return competitionJpaRepository.save(entity).toDomain();
                })
                .orElseGet(() -> {
                    // 없으면 새로 저장
                    CompetitionEntity entity = CompetitionEntity.from(competition);
                    return competitionJpaRepository.save(entity).toDomain();
                });
    }

    @Override
    public Optional<Competition> findById(UUID id) {
        return competitionJpaRepository.findById(id)
                .map(CompetitionEntity::toDomain);
    }

    @Override
    public Optional<Competition> findByIdForUpdate(UUID id) {
        return competitionJpaRepository.findByIdForUpdate(id)
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

    @Override
    public void delete(Competition competition, String deletedBy) {
        CompetitionEntity entity = competitionJpaRepository.findById(competition.getCompetitionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대회입니다."));
        entity.softDelete(deletedBy);
    }
}
