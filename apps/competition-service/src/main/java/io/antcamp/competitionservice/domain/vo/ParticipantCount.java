package io.antcamp.competitionservice.domain.vo;

/**
 * 참가 인원 VO (순수 도메인 - JPA 의존성 없음)
 */
public class ParticipantCount {

    private final int min;
    private final int max;
    private final int current;

    private ParticipantCount(int min, int max, int current) {
        if (min > max) {
            throw new IllegalArgumentException("최소 인원은 최대 인원보다 클 수 없습니다.");
        }
        if (min < 0 || current < 0) {
            throw new IllegalArgumentException("참가 인원은 0 이상이어야 합니다.");
        }
        this.min = min;
        this.max = max;
        this.current = current;
    }

    public static ParticipantCount of(int min, int max) {
        return new ParticipantCount(min, max, 0);
    }

    public static ParticipantCount of(int min, int max, int current) {
        return new ParticipantCount(min, max, current);
    }


    public boolean isFull() {
        return current >= max;
    }

    public boolean isMetMinimum() {
        return current >= min;
    }

    public ParticipantCount increment() {
        if (isFull()) {
            throw new IllegalStateException("최대 참가 인원을 초과할 수 없습니다.");
        }
        return new ParticipantCount(min, max, current + 1);
    }

    public ParticipantCount decrement() {
        if (current <= 0) {
            throw new IllegalStateException("취소할 신청 인원이 없습니다.");
        }
        return new ParticipantCount(min, max, current - 1);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getCurrent() {
        return current;
    }
}
