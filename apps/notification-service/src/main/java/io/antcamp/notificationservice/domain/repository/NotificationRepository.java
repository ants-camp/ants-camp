package io.antcamp.notificationservice.domain.repository;

import io.antcamp.notificationservice.domain.model.Notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID notificationId);

    boolean existsSentByDeduplicationKey(String deduplicationKey);
}
