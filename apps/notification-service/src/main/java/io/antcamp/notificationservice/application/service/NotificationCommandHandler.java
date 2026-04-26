package io.antcamp.notificationservice.application.service;

import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.model.ResolutionAction;
import io.antcamp.notificationservice.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCommandHandler {

    private final NotificationRepository notificationRepository;

    /**
     * PESSIMISTIC_WRITE 락 획득 → 액션 기록 → 커밋 순서로 짧게 유지.
     * 중복 처리 시 IllegalStateException, 유효하지 않은 입력 시 IllegalArgumentException.
     */
    @Transactional
    public Notification recordAction(UUID notificationId, ResolutionAction action, String userEmail) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림을 찾을 수 없습니다: " + notificationId));
        notification.recordAction(action, userEmail);
        Notification saved = notificationRepository.save(notification);
        log.info("액션 기록 완료: notificationId={}, action={}", notificationId, action);
        return saved;
    }

    @Transactional
    public void markActionFailed(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.markActionFailed();
            notificationRepository.save(notification);
        });
    }
}