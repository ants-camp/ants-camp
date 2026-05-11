package io.antcamp.competitionservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CompetitionChangeNotice {

    private final UUID noticeId;
    private final UUID competitionId;
    private final CompetitionStatus competitionStatus;
    private final String beforeContents;
    private final String afterContents;
    private final String reason;
    private final LocalDateTime createdAt;

    private CompetitionChangeNotice(
            UUID competitionId,
            CompetitionStatus competitionStatus,
            String beforeContents,
            String afterContents,
            String reason
    ) {
        validate(beforeContents, afterContents, reason);
        this.noticeId = UUID.randomUUID();
        this.competitionId = competitionId;
        this.competitionStatus = competitionStatus;
        this.beforeContents = beforeContents;
        this.afterContents = afterContents;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    private CompetitionChangeNotice(
            UUID noticeId,
            UUID competitionId,
            CompetitionStatus competitionStatus,
            String beforeContents,
            String afterContents,
            String reason,
            LocalDateTime createdAt
    ) {
        this.noticeId = noticeId;
        this.competitionId = competitionId;
        this.competitionStatus = competitionStatus;
        this.beforeContents = beforeContents;
        this.afterContents = afterContents;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static CompetitionChangeNotice create(
            UUID competitionId,
            CompetitionStatus competitionStatus,
            String beforeContents,
            String afterContents,
            String reason
    ) {
        return new CompetitionChangeNotice(
                competitionId,
                competitionStatus,
                beforeContents,
                afterContents,
                reason
        );
    }

    public static CompetitionChangeNotice reconstitute(
            UUID noticeId,
            UUID competitionId,
            CompetitionStatus competitionStatus,
            String beforeContents,
            String afterContents,
            String reason,
            LocalDateTime createdAt
    ) {
        return new CompetitionChangeNotice(
                noticeId,
                competitionId,
                competitionStatus,
                beforeContents,
                afterContents,
                reason,
                createdAt
        );
    }

    private static void validate(
            String beforeContents,
            String afterContents,
            String reason
    ) {
        if (beforeContents == null || beforeContents.isBlank()) {
            throw new BusinessException(ErrorCode.COMPETITION_CHANGE_NOTICE_BEFORE_CONTENTS_REQUIRED);
        }
        if (afterContents == null || afterContents.isBlank()) {
            throw new BusinessException(ErrorCode.COMPETITION_CHANGE_NOTICE_AFTER_CONTENTS_REQUIRED);
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.COMPETITION_CHANGE_NOTICE_REASON_REQUIRED);
        }
    }
}
