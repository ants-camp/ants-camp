package io.antcamp.competitionservice.domain.vo;

import java.time.LocalDateTime;

/**
 * 대회 진행 기간 VO (순수 도메인 - JPA 의존성 없음)
 */
public class CompetitionPeriod {

    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    private CompetitionPeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("시작일과 종료일은 필수입니다.");
        }
        if (startAt.isAfter(endAt)) {
            throw new IllegalArgumentException("대회 시작일은 종료일보다 이전이어야 합니다.");
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
