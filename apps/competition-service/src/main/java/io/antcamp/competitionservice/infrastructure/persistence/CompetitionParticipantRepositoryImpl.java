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

    @Override
    public CompetitionParticipant save(CompetitionParticipant participant) {
        return competitionParticipantJpaRepository.save(CompetitionParticipantEntity.from(participant)).toDomain();
    }

    @Override
    public Optional<CompetitionParticipant> findByUserIdAndCompetitionId(UUID userId, UUID competitionId) {
        return competitionParticipantJpaRepository
                .findByUserIdAndCompetitionIdWithLock(userId, competitionId)
                .map(CompetitionParticipantEntity::toDomain);
    }

    @Override
    public void delete(CompetitionParticipant participant, String deletedBy) {
        CompetitionParticipantEntity entity = competitionParticipantJpaRepository
                .findByUserIdAndCompetitionIdWithLock(participant.getUserId(), participant.getCompetitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        entity.softDelete(deletedBy);
    }

    @Override
    public List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId) {
        return competitionParticipantJpaRepository.findAllByCompetitionId(competitionId).stream()
                .map(CompetitionParticipantEntity::toDomain)
                .toList();
    }
}
