package io.antcamp.competitionservice.domain.model;

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
        // 객체가 생성되기 직전에 검증한다. ( 검증위치를 생성자 내부로 이동 )
        validate(name, type, description, firstSeed, registerPeriod, competitionPeriod, participantCount);
    }

    /**
     * 영속 데이터로부터 도메인 객체를 복원할 때 사용하는 생성자. 외부에서는 from() 정적 팩토리 메서드를 통해서만 호출 가능.
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
        // 객체가 생성되기 직전에 검증한다. ( 검증위치를 생성자 내부로 이동 )
        validate(name, type, description, firstSeed, registerPeriod, competitionPeriod, participantCount);
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
        // 빌더 호출 전에는 검증하지 않는다.
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

    public static Competition from(
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
        // 생성자 내부에서 검증을 하기에 from메서드에서는 검증할 필요가 없다.
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
            throw new IllegalStateException("현재 참가 신청이 불가능한 대회입니다.");
        }
        this.participantCount = participantCount.increment();
    }

    public void cancelRegister() {
        if (status != CompetitionStatus.PREPARING) {
            throw new IllegalStateException("대회가 시작된 이후에는 참가 취소가 불가능합니다.");
        }
        this.participantCount = participantCount.decrement();
    }

    public void startCompetition() {
        if (status != CompetitionStatus.PREPARING) {
            throw new IllegalStateException("준비 중인 대회만 시작할 수 있습니다.");
        }
        if (!competitionPeriod.isOngoing()) {
            throw new IllegalStateException("대회 진행 기간이 아닙니다.");
        }
        if (!participantCount.isMetMinimum()) {
            throw new IllegalStateException(
                    String.format("최소 참가 인원(%d명)을 충족하지 못했습니다. 현재: %d명",
                            participantCount.getMin(), participantCount.getCurrent())
            );
        }
        this.status = CompetitionStatus.ONGOING;
    }

    public void finishCompetition() {
        if (status != CompetitionStatus.ONGOING) {
            throw new IllegalStateException("진행 중인 대회만 종료할 수 있습니다.");
        }
        if (competitionPeriod.isOngoing()) {
            throw new IllegalStateException("대회 진행 기간 중에는 종료할 수 없습니다.");
        }
        this.status = CompetitionStatus.FINISHED;
    }

    public void cancelCompetition() {
        if (status == CompetitionStatus.FINISHED) {
            throw new IllegalStateException("이미 종료된 대회는 취소할 수 없습니다.");
        }
        this.status = CompetitionStatus.CANCELED;
    }

    public void publish() {
        if (this.isReadable) {
            throw new IllegalStateException("이미 게시된 대회입니다.");
        }
        if (this.status != CompetitionStatus.PREPARING) {
            throw new IllegalStateException("준비 중인 대회만 게시할 수 있습니다.");
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
            throw new IllegalStateException("종료되거나 취소된 대회는 수정할 수 없습니다.");
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
        if (registerPeriod.getEndAt().isAfter(competitionPeriod.getStartAt())) {
            throw new IllegalArgumentException("신청 종료일은 대회 시작일 이전(또는 동일)이어야 합니다.");
        }
        if (participantCount == null) {
            throw new IllegalArgumentException("참가 인원 정보는 필수입니다.");
        }
    }
}
