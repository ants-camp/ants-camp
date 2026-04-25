package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompetitionRepository {
    Competition save(Competition competition);

    Optional<Competition> findById(UUID id);

    /**
     * 전체 대회 목록 조회
     */
    Page<Competition> findAll(Pageable pageable);

    /**
     * 대회 상태별 목록 조회
     */
    Page<Competition> findAllByCompetitionStatus(CompetitionStatus status, Pageable pageable);

    void delete(Competition competition, String deletedBy);  // 추가

}
