package io.antcamp.competitionservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;

/**
 * 참가 인원 VO (순수 도메인 - JPA 의존성 없음)
 */
public class ParticipantCount {

    private final int min;
    private final int max;
    private final int current;

    private ParticipantCount(int min, int max, int current) {
        if (min > max) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (min < 0 || current < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
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
            throw new BusinessException(ErrorCode.COMPETITION_NOT_REGISTERABLE);
        }
        return new ParticipantCount(min, max, current + 1);
    }

    public ParticipantCount decrement() {
        if (current <= 0) {
            throw new BusinessException(ErrorCode.COMPETITION_INVALID_STATUS);
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
