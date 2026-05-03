package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.infrastructure.entity.CompetitionEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionJpaRepository extends JpaRepository<CompetitionEntity, UUID> {

    Page<CompetitionEntity> findAllByStatus(CompetitionStatus status, Pageable pageable);

    /**
     * 비관적 락(SELECT FOR UPDATE)으로 Competition 조회. 참가 신청/취소 시 participantCount의 동시성 제어를 위해 사용.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompetitionEntity c WHERE c.competitionId = :id")
    Optional<CompetitionEntity> findByIdWithLock(@Param("id") UUID id);

    /**
     * 진행 중인(ONGOING) 대회 ID 목록만 조회. 틱 이벤트 발행 시 전체 엔티티 로딩을 피하기 위해 ID만 select.
     */
    @Query("SELECT c.competitionId FROM CompetitionEntity c WHERE c.status = :status")
    List<UUID> findAllOngoingIds(@Param("status") CompetitionStatus status);
}
