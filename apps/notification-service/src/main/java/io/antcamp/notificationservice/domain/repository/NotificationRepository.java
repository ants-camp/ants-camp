package io.antcamp.notificationservice.domain.repository;

import io.antcamp.notificationservice.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID notificationId);

    Optional<Notification> findByIdForUpdate(UUID notificationId);

    Page<Notification> search(NotificationSearchCriteria criteria, Pageable pageable);
}
