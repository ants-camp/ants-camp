package io.antcamp.notificationservice.domain.model;

import io.antcamp.notificationservice.domain.exception.AlreadyHandledException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Notification {

    private UUID notificationId;
    private final String channelId;
    private final String job;
    private final AlertSource source;
    private final String deduplicationKey;
    private final AlertSeverity severity;
    private final String title;
    private final String content;
    private final String rawPayload;   // Prometheus alert 원본 JSON
    private final String aiAnalysis;   // LLM 분석 결과
    private AlertStatus status;
    private String slackMessageTs;
    private ResolutionAction actionButton;
    private String actionUserEmail;

    public static Notification restore(
            UUID notificationId,
            String channelId,
            String job,
            AlertSource source,
            String deduplicationKey,
            AlertSeverity severity,
            String title,
            String content,
            String rawPayload,
            String aiAnalysis,
            AlertStatus status,
            String slackMessageTs,
            ResolutionAction actionButton,
            String actionUserEmail
    ) {
        return Notification.builder()
                .notificationId(notificationId)
                .channelId(channelId)
                .job(job)
                .source(source)
                .deduplicationKey(deduplicationKey)
                .severity(severity)
                .title(title)
                .content(content)
                .rawPayload(rawPayload)
                .aiAnalysis(aiAnalysis)
                .status(status)
                .slackMessageTs(slackMessageTs)
                .actionButton(actionButton)
                .actionUserEmail(actionUserEmail)
                .build();
    }

    public static Notification create(
            String channelId,
            String job,
            AlertSource source,
            String deduplicationKey,
            AlertSeverity severity,
            String title,
            String content,
            String rawPayload,
            String aiAnalysis
    ) {
        validateChannelId(channelId);
        validateDeduplicationKey(deduplicationKey);
        validateTitle(title);
        validateContent(content);

        return Notification.builder()
                .channelId(channelId)
                .job(job)
                .source(source)
                .deduplicationKey(deduplicationKey)
                .severity(severity)
                .title(title)
                .content(content)
                .rawPayload(rawPayload)
                .aiAnalysis(aiAnalysis)
                .status(AlertStatus.PENDING)
                .build();
    }

    public void markAsSent(String slackMessageTs) {
        if (this.status != AlertStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 전송 완료 처리할 수 있습니다.");
        }
        this.status = AlertStatus.SENT;
        this.slackMessageTs = slackMessageTs;
    }

    public void markAsFailed() {
        if (this.status != AlertStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 실패 처리할 수 있습니다.");
        }
        this.status = AlertStatus.FAILED;
    }

    public void recordAction(ResolutionAction button, String userEmail) {
        if (this.status != AlertStatus.SENT) {
            throw new IllegalStateException("전송 완료된 메시지에만 액션을 기록할 수 있습니다.");
        }
        if (this.actionButton != null) {
            throw new AlreadyHandledException("이미 처리된 알림입니다.");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException("액션을 수행한 유저 이메일은 비어있을 수 없습니다.");
        }
        this.actionButton = button;
        this.actionUserEmail = userEmail;
    }

    public void markActionFailed() {
        this.status = AlertStatus.ACTION_FAILED;
    }

    private static void validateChannelId(String channelId) {
        if (channelId == null || channelId.isBlank()) throw new IllegalArgumentException("채널 ID는 비어있을 수 없습니다.");
        if (channelId.length() > 100) throw new IllegalArgumentException("채널 ID는 100자를 초과할 수 없습니다.");
    }

    private static void validateDeduplicationKey(String deduplicationKey) {
        if (deduplicationKey == null || deduplicationKey.isBlank())
            throw new IllegalArgumentException("중복 억제 키는 비어있을 수 없습니다.");
        if (deduplicationKey.length() > 255) throw new IllegalArgumentException("중복 억제 키는 255자를 초과할 수 없습니다.");
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        if (title.length() > 255) throw new IllegalArgumentException("제목은 255자를 초과할 수 없습니다.");
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("내용은 비어있을 수 없습니다.");
    }
}
