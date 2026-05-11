package io.antcamp.competitionservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 대회 도메인 클래스 (순수 도메인 모델)
 */
@Getter
public class Competition {

    private final UUID competitionId;
    private String name;
    private CompetitionType type;
    private CompetitionStatus status;
    private String description;
    private int firstSeed;
    private boolean isReadable;
    private RegisterPeriod registerPeriod;
    private CompetitionPeriod competitionPeriod;
    private ParticipantCount participantCount;

    /**
     * 신규 대회 생성용 빌더 생성자. 외부에서는 createCompetition() 정적 팩토리 메서드를 통해서만 호출 가능.
     */
    @Builder(access = AccessLevel.PRIVATE)
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
        this.isReadable = false;
        this.registerPeriod = registerPeriod;
        this.competitionPeriod = competitionPeriod;
        this.participantCount = participantCount;
        validate();
    }

    /**
     * 영속 데이터로부터 도메인 객체를 복원할 때 사용하는 생성자. 외부에서는 reconstitute() 정적 팩토리 메서드를 통해서만 호출 가능.
     */
    private Competition(
            UUID competitionId,
            String name,
            CompetitionType type,
            CompetitionStatus status,
            String description,
            int firstSeed,
            boolean isReadable,
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
        this.isReadable = isReadable;
        this.registerPeriod = registerPeriod;
        this.competitionPeriod = competitionPeriod;
        this.participantCount = participantCount;
        validate();
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
        return Competition.builder()
                .name(name)
                .type(type)
                .description(description)
                .firstSeed(firstSeed)
                .registerPeriod(registerPeriod)
                .competitionPeriod(competitionPeriod)
                .participantCount(participantCount)
                .build();
    }

    public static Competition reconstitute(
            UUID competitionId,
            String name,
            CompetitionType type,
            CompetitionStatus status,
            String description,
            int firstSeed,
            boolean isReadable,
            RegisterPeriod registerPeriod,
            CompetitionPeriod competitionPeriod,
            ParticipantCount participantCount
    ) {
        return new Competition(
                competitionId,
                name,
                type,
                status,
                description,
                firstSeed,
                isReadable,
                registerPeriod,
                competitionPeriod,
                participantCount
        );
    }

    // ─── 도메인 행위 ─────────────────────────────────────────────────────

    public void register() {
        if (!isRegisterable()) {
            throw new BusinessException(ErrorCode.COMPETITION_NOT_REGISTERABLE);
        }
        this.participantCount = participantCount.increment();
    }

    public void cancelRegister() {
        if (status != CompetitionStatus.PREPARING) {
            throw new BusinessException(ErrorCode.COMPETITION_INVALID_STATUS);
        }
        this.participantCount = participantCount.decrement();
    }

    public void startCompetition() {
        if (status != CompetitionStatus.PREPARING) {
            throw new BusinessException(ErrorCode.COMPETITION_INVALID_STATUS);
        }
        // [변경] 시간 검증 제거 - 운영자 수동 트리거 정책
        // if (!competitionPeriod.isOngoing()) {
        //     throw new BusinessException(ErrorCode.COMPETITION_INVALID_STATUS);
        // }
        if (!participantCount.isMetMinimum()) {
            throw new BusinessException(ErrorCode.COMPETITION_MIN_PARTICIPANTS_NOT_MET);
        }
        this.status = CompetitionStatus.ONGOING;
    }

    public void finishCompetition() {
        if (status != CompetitionStatus.ONGOING) {
            throw new BusinessException(ErrorCode.COMPETITION_INVALID_STATUS);
        }
        // [변경] 시간 검증 제거 - 운영자 수동 트리거 정책
        // if (competitionPeriod.isOngoing()) {
        //     throw new BusinessException(ErrorCode.COMPETITION_INVALID_STATUS);
        // }
        this.status = CompetitionStatus.FINISHED;
    }

    public void cancelCompetition() {
        if (status == CompetitionStatus.FINISHED) {
            throw new BusinessException(ErrorCode.COMPETITION_ALREADY_FINISHED);
        }
        this.status = CompetitionStatus.CANCELED;
    }

    public void publish() {
        if (this.isReadable) {
            throw new BusinessException(ErrorCode.COMPETITION_ALREADY_PUBLISHED);
        }
        if (this.status != CompetitionStatus.PREPARING) {
            throw new BusinessException(ErrorCode.COMPETITION_INVALID_STATUS);
        }
        this.isReadable = true;
    }

    public void updateInfo(
            String name,
            String description,
            RegisterPeriod registerPeriod,
            CompetitionPeriod competitionPeriod,
            ParticipantCount participantCount
    ) {
        if (this.status == CompetitionStatus.FINISHED || this.status == CompetitionStatus.CANCELED) {
            throw new BusinessException(ErrorCode.COMPETITION_CANNOT_UPDATE);
        }
        this.name = name;
        this.description = description;
        this.registerPeriod = registerPeriod;
        this.competitionPeriod = competitionPeriod;
        this.participantCount = participantCount;
    }

    private boolean isRegisterable() {
        return status == CompetitionStatus.PREPARING
                && isReadable
                && registerPeriod.isOpen()
                && !participantCount.isFull();
    }

    /**
     * 객체 생성 시점에 필수 필드 및 도메인 규칙을 검증한다. 빌더 방식의 단점(필수값 누락 시에도 객체가 생성되는 문제)을 보완.
     */
    private void validate() {
        if (registerPeriod == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (competitionPeriod == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (participantCount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (registerPeriod.getEndAt().isAfter(competitionPeriod.getStartAt())) {
            throw new BusinessException(ErrorCode.COMPETITION_REGISTER_END_AFTER_COMPETITION_START);
        }
    }
}
