package io.antcamp.competitionservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import java.time.LocalDateTime;

/**
 * 대회 진행 기간 VO (순수 도메인 - JPA 의존성 없음)
 */
public class CompetitionPeriod {

    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    private CompetitionPeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt.isAfter(endAt)) {
            throw new BusinessException(ErrorCode.COMPETITION_PERIOD_START_AFTER_END);
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static CompetitionPeriod of(LocalDateTime startAt, LocalDateTime endAt) {
        return new CompetitionPeriod(startAt, endAt);
    }

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt);
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
}
