package io.antcamp.notificationservice.application.service;

import io.antcamp.notificationservice.application.dto.response.NotificationDetailResponse;
import io.antcamp.notificationservice.application.dto.response.NotificationSummaryResponse;
import io.antcamp.notificationservice.domain.exception.NotificationException;
import io.antcamp.notificationservice.domain.repository.NotificationRepository;
import io.antcamp.notificationservice.domain.repository.NotificationSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private static final int PAGE_SIZE = 30;

    private final NotificationRepository notificationRepository;

    public Page<NotificationSummaryResponse> search(NotificationSearchCriteria criteria, int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.search(criteria, pageRequest)
                .map(NotificationSummaryResponse::of);
    }

    public NotificationDetailResponse findById(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .map(NotificationDetailResponse::of)
                .orElseThrow(NotificationException::notFound);
    }
}
