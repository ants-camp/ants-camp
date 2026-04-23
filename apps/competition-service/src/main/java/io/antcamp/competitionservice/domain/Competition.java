package io.antcamp.competitionservice.domain;

import io.antcamp.competitionservice.domain.vo.CompetitionPeriod;
import io.antcamp.competitionservice.domain.vo.ParticipantCount;
import io.antcamp.competitionservice.domain.vo.RegisterPeriod;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 대회 도메인 클래스 (순수 도메인 모델 - JPA 의존성 없음)
 */
@Getter
public class Competition {

    private final UUID competitionId;
    private String name;
    private CompetitionType type;
    private CompetitionStatus status;
    private String description;
    private int firstSeed;
    private RegisterPeriod registerPeriod;
    private CompetitionPeriod competitionPeriod;
    private ParticipantCount participantCount;

    @Builder(builderMethodName = "createBuilder", access = AccessLevel.PRIVATE)
    private Competition(
            String name,
            CompetitionType type,
            String description,
            int firstSeed,
            RegisterPeriod registerPeriod,
            CompetitionPeriod competitionPeriod,
            ParticipantCount participantCount
    ) {
        this.competitionId = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.status = CompetitionStatus.PREPARING;
        this.description = description;
        this.firstSeed = firstSeed;
        this.registerPeriod = registerPeriod;
        this.competitionPeriod = competitionPeriod;
        this.participantCount = participantCount;
    }

    @Builder(builderMethodName = "fromBuilder", access = AccessLevel.PRIVATE)
    private Competition(
            UUID competitionId,
            String name,
            CompetitionType type,
            CompetitionStatus status,
            String description,
            int firstSeed,
            RegisterPeriod registerPeriod,
            CompetitionPeriod competitionPeriod,
            ParticipantCount participantCount
    ) {
        this.competitionId = competitionId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.description = description;
        this.firstSeed = firstSeed;
        this.registerPeriod = registerPeriod;
        this.competitionPeriod = competitionPeriod;
        this.participantCount = participantCount;
    }

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────────

    public static Competition createCompetition(
            String name,
            CompetitionType type,
            String description,
            int firstSeed,
            RegisterPeriod registerPeriod,
            CompetitionPeriod competitionPeriod,
            ParticipantCount participantCount
    ) {
        validate(name, type, description, firstSeed, registerPeriod, competitionPeriod, participantCount);

        return Competition.createBuilder()
                .name(name)
                .type(type)
                .description(description)
                .firstSeed(firstSeed)
                .registerPeriod(registerPeriod)
                .competitionPeriod(competitionPeriod)
                .participantCount(participantCount)
                .build();
    }

    public static Competition from(
            UUID competitionId,
            String name,
            CompetitionType type,
            CompetitionStatus status,
            String description,
            int firstSeed,
            RegisterPeriod registerPeriod,
            CompetitionPeriod competitionPeriod,
            ParticipantCount participantCount
    ) {
        return Competition.fromBuilder()
                .competitionId(competitionId)
                .name(name)
                .type(type)
                .status(status)
                .description(description)
                .firstSeed(firstSeed)
                .registerPeriod(registerPeriod)
                .competitionPeriod(competitionPeriod)
                .participantCount(participantCount)
                .build();
    }
    // ─── 도메인 행위 ─────────────────────────────────────────────────────

    // 대회 신청
    public void register() {
        if (!isRegisterable()) {
            throw new IllegalStateException("현재 참가 신청이 불가능한 대회입니다.");
        }
        this.participantCount = participantCount.increment();
    }

    // 대회 신청 취소
    public void cancelRegister() {
        if (status != CompetitionStatus.PREPARING) {
            throw new IllegalStateException("대회가 시작된 이후에는 참가 취소가 불가능합니다.");
        }
        this.participantCount = participantCount.decrement();
    }


    // 대회 시작
    public void startCompetition() {
        if (!participantCount.isMetMinimum()) {
            throw new IllegalStateException(
                    String.format("최소 참가 인원(%d명)을 충족하지 못했습니다. 현재: %d명",
                            participantCount.getMin(), participantCount.getCurrent())
            );
        }
        if (status != CompetitionStatus.PREPARING) {
            throw new IllegalStateException("준비 중인 대회만 시작할 수 있습니다.");
        }
        this.status = CompetitionStatus.ONGOING;
    }

    // 대회 종료
    public void finishCompetition() {
        if (status != CompetitionStatus.ONGOING) {
            throw new IllegalStateException("진행 중인 대회만 종료할 수 있습니다.");
        }
        this.status = CompetitionStatus.FINISHED;
    }

    // 대회 취소
    public void cancelCompetition() {
        if (status == CompetitionStatus.FINISHED) {
            throw new IllegalStateException("이미 종료된 대회는 취소할 수 없습니다.");
        }
        this.status = CompetitionStatus.CANCELED;
    }

    private boolean isRegisterable() {
        return status == CompetitionStatus.PREPARING
                && registerPeriod.isOpen()
                && !participantCount.isFull();
    }

    private static void validate(
            String name,
            CompetitionType type,
            String description,
            int firstSeed,
            RegisterPeriod registerPeriod,
            CompetitionPeriod competitionPeriod,
            ParticipantCount participantCount
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("대회명은 필수입니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("대회 타입은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("대회 설명은 필수입니다.");
        }
        if (firstSeed <= 0) {
            throw new IllegalArgumentException("초기 시드머니는 0보다 커야 합니다.");
        }
        if (registerPeriod == null) {
            throw new IllegalArgumentException("신청 기간은 필수입니다.");
        }
        if (competitionPeriod == null) {
            throw new IllegalArgumentException("대회 기간은 필수입니다.");
        }
        if (participantCount == null) {
            throw new IllegalArgumentException("참가 인원 정보는 필수입니다.");
        }
    }
}
