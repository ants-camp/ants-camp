package io.antcamp.notificationservice.infrastructure.persistence;

import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.repository.NotificationRepository;
import io.antcamp.notificationservice.domain.repository.NotificationSearchCriteria;
import io.antcamp.notificationservice.infrastructure.entity.NotificationEntity;
import io.antcamp.notificationservice.infrastructure.persistence.query.NotificationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;
    private final NotificationQueryRepository notificationQueryRepository;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = NotificationEntity.from(notification);
        NotificationEntity saved = jpaNotificationRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Notification> findById(UUID notificationId) {
        return jpaNotificationRepository.findByIdReadOnly(notificationId)
                .map(NotificationEntity::toDomain);
    }

    @Override
    public Optional<Notification> findByIdForUpdate(UUID notificationId) {
        return jpaNotificationRepository.findByIdForUpdate(notificationId)
                .map(NotificationEntity::toDomain);
    }

    @Override
    public Page<Notification> search(NotificationSearchCriteria criteria, Pageable pageable) {
        return notificationQueryRepository.search(criteria, pageable)
                .map(NotificationEntity::toDomain);
    }
}