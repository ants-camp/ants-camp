package io.antcamp.notificationservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.notificationservice.domain.model.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "p_slack_messages")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slack_message_id", updatable = false, nullable = false)
    private UUID notificationId;

    @Column(name = "slack_channel_id", nullable = false, length = 100)
    private String channelId;

    @Column(name = "job", nullable = false, length = 100)
    private String job;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private AlertSource source;

    @Column(name = "deduplication_key", nullable = false)
    private String deduplicationKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlertStatus status;

    @Column(name = "slack_message_ts", length = 50)
    private String slackMessageTs;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_action", length = 50)
    private ResolutionAction resolutionAction;

    @Column(name = "action_user_email", length = 100)
    private String actionUserEmail;

    public static NotificationEntity from(Notification domain) {
        return NotificationEntity.builder()
                .notificationId(domain.getNotificationId())
                .channelId(domain.getChannelId())
                .job(domain.getJob())
                .source(domain.getSource())
                .deduplicationKey(domain.getDeduplicationKey())
                .severity(domain.getSeverity())
                .title(domain.getTitle())
                .content(domain.getContent())
                .payload(domain.getRawPayload())
                .aiAnalysis(domain.getAiAnalysis())
                .status(domain.getStatus())
                .slackMessageTs(domain.getSlackMessageTs())
                .resolutionAction(domain.getActionButton())
                .actionUserEmail(domain.getActionUserEmail())
                .build();
    }

    public Notification toDomain() {
        return Notification.builder()
                .notificationId(this.notificationId)
                .channelId(this.channelId)
                .job(this.job)
                .source(this.source)
                .deduplicationKey(this.deduplicationKey)
                .severity(this.severity)
                .title(this.title)
                .content(this.content)
                .rawPayload(this.payload)
                .aiAnalysis(this.aiAnalysis)
                .status(this.status)
                .slackMessageTs(this.slackMessageTs)
                .actionButton(this.resolutionAction)
                .actionUserEmail(this.actionUserEmail)
                .build();
    }
}