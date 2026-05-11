package io.antcamp.competitionservice.domain.repository;

import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompetitionRepository {

    // Create / Update
    Competition save(Competition competition);

    // Read
    Optional<Competition> findById(UUID id);

    /**
     * 비관적 락으로 Competition 조회. 참가자 수 변경 시(register/cancelRegister) 동시성 제어를 위해 사용.
     */
    Optional<Competition> findByIdWithLock(UUID id);

    // Delete
    void delete(Competition competition, String deletedBy);

    // Search
    Page<Competition> findAll(Pageable pageable);
    Page<Competition> findAllByCompetitionStatus(CompetitionStatus status, Pageable pageable);

    /**
     * 현재 진행 중인(ONGOING) 대회 ID 목록 조회. 1분마다 틱 이벤트 발행 시 사용.
     */
    List<UUID> findAllOngoingIds();

    /**
     * 자동 시작 대상 대회 ID 목록 조회. PREPARING 상태이며 대회 시작 시간이 현재 시각 이전인 대회.
     */
    List<UUID> findAllIdsReadyToStart();

    /**
     * 자동 종료 대상 대회 ID 목록 조회. ONGOING 상태이며 대회 종료 시간이 현재 시각 이전인 대회.
     */
    List<UUID> findAllIdsReadyToFinish();
}
