package io.antcamp.competitionservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import java.time.LocalDateTime;

/**
 * 참가 신청 기간 VO (순수 도메인 - JPA 의존성 없음)
 */
public class RegisterPeriod {

    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    private RegisterPeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (startAt.isAfter(endAt)) {
            throw new BusinessException(ErrorCode.COMPETITION_REGISTER_PERIOD_START_AFTER_END);
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static RegisterPeriod of(LocalDateTime startAt, LocalDateTime endAt) {
        return new RegisterPeriod(startAt, endAt);
    }

    public boolean isOpen() {
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
