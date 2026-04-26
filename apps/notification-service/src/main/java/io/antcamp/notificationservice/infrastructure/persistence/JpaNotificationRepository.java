package io.antcamp.notificationservice.infrastructure.persistence;

import io.antcamp.notificationservice.infrastructure.entity.NotificationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    @Query("SELECT n FROM NotificationEntity n WHERE n.notificationId = :id")
    Optional<NotificationEntity> findByIdReadOnly(@Param("id") UUID id);

    // 중복 액션 방지용 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional
    @Query("SELECT n FROM NotificationEntity n WHERE n.notificationId = :id")
    Optional<NotificationEntity> findByIdForUpdate(@Param("id") UUID id);
}
