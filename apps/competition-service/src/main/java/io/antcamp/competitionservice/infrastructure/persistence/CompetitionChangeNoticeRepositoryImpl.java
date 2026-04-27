package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import io.antcamp.competitionservice.domain.repository.CompetitionChangeNoticeRepository;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionChangeNoticeEntity;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompetitionChangeNoticeRepositoryImpl implements CompetitionChangeNoticeRepository {

    private final CompetitionChangeNoticeJpaRepository competitionChangeNoticeJpaRepository;

    @Override
    public CompetitionChangeNotice save(CompetitionChangeNotice notice) {
        CompetitionChangeNoticeEntity entity = CompetitionChangeNoticeEntity.from(notice);
        CompetitionChangeNoticeEntity saved = competitionChangeNoticeJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<CompetitionChangeNotice> findAllByCompetitionId(UUID competitionId) {
        return competitionChangeNoticeJpaRepository.findAllByCompetitionId(competitionId)
                .stream()
                .map(CompetitionChangeNoticeEntity::toDomain)
                .toList();
    }
}
