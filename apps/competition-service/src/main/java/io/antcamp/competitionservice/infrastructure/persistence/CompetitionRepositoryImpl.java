package io.antcamp.competitionservice.infrastructure.persistence;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CompetitionRepositoryImpl implements CompetitionRepository {

    private final CompetitionJpaRepository competitionJpaRepository;

    // ── Create / Update ───────────────────────────────────────────────────────

    @Override
    public Competition save(Competition competition) {
        log.info("대회 리포지토리 구현체 - Competition save: {}", competition);
        return competitionJpaRepository.findById(competition.getCompetitionId())
                .map(entity -> {
                    entity.update(competition);
                    return competitionJpaRepository.save(entity).toDomain();
                })
                .orElseGet(() -> {
                    CompetitionEntity entity = CompetitionEntity.from(competition);
                    log.info("대회 리포지토리 구현체 - 새로운 엔티티 저장 직전");
                    return competitionJpaRepository.save(entity).toDomain();
                });
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public Optional<Competition> findById(UUID id) {
        return competitionJpaRepository.findById(id)
                .map(CompetitionEntity::toDomain);
    }

    @Override
    public Optional<Competition> findByIdWithLock(UUID id) {
        return competitionJpaRepository.findByIdWithLock(id)
                .map(CompetitionEntity::toDomain);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(Competition competition, String deletedBy) {
        CompetitionEntity entity = competitionJpaRepository.findById(competition.getCompetitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        entity.softDelete(deletedBy);
    }

    // ── Search ────────────────────────────────────────────────────────────────

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
    public List<UUID> findAllOngoingIds() {
        return competitionJpaRepository.findAllOngoingIds(CompetitionStatus.ONGOING);
    }

    @Override
    public List<UUID> findAllIdsReadyToStart() {
        return competitionJpaRepository.findAllIdsReadyToStart(LocalDateTime.now());
    }

    @Override
    public List<UUID> findAllIdsReadyToFinish() {
        return competitionJpaRepository.findAllIdsReadyToFinish(LocalDateTime.now());
    }
}
