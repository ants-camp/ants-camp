package io.antcamp.assistantservice.infrastructure.scheduler;

import io.antcamp.assistantservice.application.service.RagApplicationService;
import io.antcamp.assistantservice.domain.repository.ChatSessionRepository;
import io.antcamp.assistantservice.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatReconciler {

    private static final int PENDING_TIMEOUT_MINUTES = 5;

    private final ChatSessionRepository chatSessionRepository;
    private final RagApplicationService ragApplicationService;

    // 봇 응답 없이 PENDING 상태로 남은 유저 메시지 재처리
    @Scheduled(fixedDelay = 60_000)
    public void reconcile() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(PENDING_TIMEOUT_MINUTES);
        List<ChatMessage> pendingMessages = chatSessionRepository.findPendingUserMessages(threshold);
        if (pendingMessages.isEmpty()) return;

        log.warn("미응답 메시지 재처리 시작: {}건", pendingMessages.size());
        for (ChatMessage message : pendingMessages) {
            try {
                ragApplicationService.retryPendingMessage(message);
                log.info("미응답 메시지 재처리 완료: chatMessageId={}", message.getChatMessageId());
            } catch (Exception e) {
                log.error("미응답 메시지 재처리 실패: chatMessageId={}", message.getChatMessageId(), e);
            }
        }
    }
}