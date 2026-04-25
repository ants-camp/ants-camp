package io.antcamp.competitionservice.infrastructure.persistence;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.domain.model.JoinHistory;
import io.antcamp.competitionservice.domain.repository.JoinHistoryRepository;
import io.antcamp.competitionservice.infrastructure.entity.JoinHistoryEntity;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JoinHistoryRepositoryImpl implements JoinHistoryRepository {

    private final JoinHistoryJpaRepository joinHistoryJpaRepository;

    @Override
    public JoinHistory save(JoinHistory joinHistory) {
        return joinHistoryJpaRepository.save(JoinHistoryEntity.from(joinHistory)).toDomain();
    }

    @Override
    public Optional<JoinHistory> findByUserIdAndCompetitionId(UUID userId, UUID competitionId) {
        return joinHistoryJpaRepository
                .findByUserIdAndCompetitionIdWithLock(userId, competitionId)
                .map(JoinHistoryEntity::toDomain);
    }

    @Override
    public void delete(JoinHistory joinHistory, String deletedBy) {
        JoinHistoryEntity entity = joinHistoryJpaRepository
                .findByUserIdAndCompetitionIdWithLock(joinHistory.getUserId(), joinHistory.getCompetitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        entity.softDelete(deletedBy);
    }
}
