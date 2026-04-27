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
     * 비관적 락으로 Competition 조회. 참가자 수 변경 시(register/cancelRegister) 동시성 제어를 위해 사용.
     */
    Optional<Competition> findByIdForUpdate(UUID id);   // ← 추가

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
