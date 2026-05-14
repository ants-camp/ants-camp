package io.antcamp.competitionservice.infrastructure.persistence;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import io.antcamp.competitionservice.domain.repository.CompetitionParticipantRepository;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionParticipantEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompetitionParticipantRepositoryImpl implements CompetitionParticipantRepository {

    private final CompetitionParticipantJpaRepository competitionParticipantJpaRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    public CompetitionParticipant save(CompetitionParticipant participant) {
        return competitionParticipantJpaRepository.save(CompetitionParticipantEntity.from(participant)).toDomain();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public Optional<CompetitionParticipant> findByUserIdAndCompetitionId(UUID userId, UUID competitionId) {
        return competitionParticipantJpaRepository
                .findByUserIdAndCompetitionIdWithLock(userId, competitionId)
                .map(CompetitionParticipantEntity::toDomain);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(CompetitionParticipant participant, String deletedBy) {
        CompetitionParticipantEntity entity = competitionParticipantJpaRepository
                .findByUserIdAndCompetitionIdWithLock(participant.getUserId(), participant.getCompetitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_PARTICIPANT_NOT_FOUND));
        entity.softDelete(deletedBy);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Override
    public List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId) {
        return competitionParticipantJpaRepository.findAllByCompetitionId(competitionId).stream()
                .map(CompetitionParticipantEntity::toDomain)
                .toList();
    }
}
