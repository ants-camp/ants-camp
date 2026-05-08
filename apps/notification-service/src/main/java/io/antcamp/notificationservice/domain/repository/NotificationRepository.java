package io.antcamp.notificationservice.domain.repository;

import io.antcamp.notificationservice.domain.model.Notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID notificationId);

    Optional<Notification> findByIdForUpdate(UUID notificationId);

    PageResult<Notification> search(NotificationSearchCriteria criteria, PagingRequest pagingRequest);
}
