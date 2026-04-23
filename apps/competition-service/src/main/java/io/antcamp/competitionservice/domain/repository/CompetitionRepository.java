package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetitionRepository {
    Competition save(Competition competition);

    Optional<Competition> findById(UUID id);

    /**
     * 전체 대회 목록 조회
     *
     * @return 진행 상황과 상관없이 삭제되지 않은 전체 대회 목록
     */
    List<Competition> findAll();

    /**
     * 대회 상태별 목록 조회
     *
     * @param status 조회할 대회 상태 (PREPARING, ONGOING, FINISHED, CANCELED)
     * @return 해당 상태의 대회 목록
     */
    List<Competition> findAllByCompetitionStatus(CompetitionStatus status);
}
