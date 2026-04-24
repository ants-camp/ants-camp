package io.antcamp.competitionservice.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 참가 신청 기간 VO (순수 도메인 - JPA 의존성 없음)
 */
public class RegisterPeriod {

    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    private RegisterPeriod(LocalDateTime startAt, LocalDateTime endAt) {
        Objects.requireNonNull(startAt, "신청 시작일은 필수입니다.");
        Objects.requireNonNull(endAt, "신청 종료일은 필수입니다.");
        if (startAt.isAfter(endAt)) {
            throw new IllegalArgumentException("신청 시작일은 종료일보다 이전이어야 합니다.");
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
