package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import java.util.List;
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

    /**
     * 현재 진행 중인(ONGOING) 대회 ID 목록 조회. 1분마다 틱 이벤트 발행 시 사용.
     */
    List<UUID> findAllOngoingIds();

    void delete(Competition competition, String deletedBy);  // 추가
}
