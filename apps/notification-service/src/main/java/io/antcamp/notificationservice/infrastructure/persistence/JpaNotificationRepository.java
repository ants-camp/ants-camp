package io.antcamp.notificationservice.infrastructure.persistence;

import io.antcamp.notificationservice.domain.model.AlertStatus;
import io.antcamp.notificationservice.infrastructure.entity.NotificationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    boolean existsByDeduplicationKeyAndStatusAndCreatedAtAfter(
            String deduplicationKey, AlertStatus status, LocalDateTime after);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NotificationEntity n WHERE n.notificationId = :id")
    Optional<NotificationEntity> findById(@Param("id") UUID id);
}
