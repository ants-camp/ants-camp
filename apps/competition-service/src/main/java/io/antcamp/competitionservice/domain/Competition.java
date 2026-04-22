package io.antcamp.competitionservice.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 대회 도메인 클래스 (순수 도메인 모델 - JPA 의존성 없음)
 */
public class Competition {

    private final UUID competitionId;
    private String name;
    private CompetitionType type;
    private CompetitionStatus status;
    private String description;
    private int firstSeed;
    private LocalDateTime registerStartAt;
    private LocalDateTime registerEndAt;
    private LocalDateTime competitionStartAt;
    private LocalDateTime competitionEndAt;
    private int minParticipants;
    private int maxParticipants;
    private int currentRegisters;

    // 생성자 (신규 대회 생성)
    public Competition(
            String name,
            CompetitionType type,
            String description,
            int firstSeed,
            LocalDateTime registerStartAt,
            LocalDateTime registerEndAt,
            LocalDateTime competitionStartAt,
            LocalDateTime competitionEndAt,
            int minParticipants,
            int maxParticipants
    ) {
        this.competitionId = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.status = CompetitionStatus.PREPARING; // 기본값
        this.description = description;
        this.firstSeed = firstSeed;
        this.registerStartAt = registerStartAt;
        this.registerEndAt = registerEndAt;
        this.competitionStartAt = competitionStartAt;
        this.competitionEndAt = competitionEndAt;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.currentRegisters = 0; // 기본값
    }

    // 재구성용 생성자 (DB 조회 등)
    public Competition(
            UUID competitionId,
            String name,
            CompetitionType type,
            CompetitionStatus status,
            String description,
            int firstSeed,
            LocalDateTime registerStartAt,
            LocalDateTime registerEndAt,
            LocalDateTime competitionStartAt,
            LocalDateTime competitionEndAt,
            int minParticipants,
            int maxParticipants,
            int currentRegisters
    ) {
        this.competitionId = competitionId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.description = description;
        this.firstSeed = firstSeed;
        this.registerStartAt = registerStartAt;
        this.registerEndAt = registerEndAt;
        this.competitionStartAt = competitionStartAt;
        this.competitionEndAt = competitionEndAt;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.currentRegisters = currentRegisters;
    }

    // ─── 도메인 행위 ──────────────────────────────────────────────────────

    /**
     * 참가 신청 가능 여부 확인
     */
    public boolean isRegisterable() {
        LocalDateTime now = LocalDateTime.now();
        return status == CompetitionStatus.PREPARING
                && now.isAfter(registerStartAt)
                && now.isBefore(registerEndAt)
                && currentRegisters < maxParticipants;
    }

    /**
     * 참가 신청 처리 (현재 신청 인원 증가)
     */
    public void register() {
        if (!isRegisterable()) {
            throw new IllegalStateException("현재 참가 신청이 불가능한 대회입니다.");
        }
        this.currentRegisters++;
    }

    /**
     * 참가 신청 취소 처리 (현재 신청 인원 감소)
     */
    public void cancelRegister() {
        if (this.currentRegisters <= 0) {
            throw new IllegalStateException("취소할 신청 인원이 없습니다.");
        }
        this.currentRegisters--;
    }

    /**
     * 대회 시작 (최소 참가 인원 충족 시)
     */
    public void startCompetition() {
        if (currentRegisters < minParticipants) {
            throw new IllegalStateException(
                    String.format("최소 참가 인원(%d명)을 충족하지 못했습니다. 현재: %d명", minParticipants, currentRegisters)
            );
        }
        if (status != CompetitionStatus.PREPARING) {
            throw new IllegalStateException("준비 중인 대회만 시작할 수 있습니다.");
        }
        this.status = CompetitionStatus.ONGOING;
    }

    /**
     * 대회 종료
     */
    public void finishCompetition() {
        if (status != CompetitionStatus.ONGOING) {
            throw new IllegalStateException("진행 중인 대회만 종료할 수 있습니다.");
        }
        this.status = CompetitionStatus.FINISHED;
    }

    /**
     * 대회 취소
     */
    public void cancelCompetition() {
        if (status == CompetitionStatus.FINISHED) {
            throw new IllegalStateException("이미 종료된 대회는 취소할 수 없습니다.");
        }
        this.status = CompetitionStatus.CANCELED;
    }

    // ─── Getters ────────────────────────────────────────────────────────

    public UUID getCompetitionId() {
        return competitionId;
    }

    public String getName() {
        return name;
    }

    public CompetitionType getType() {
        return type;
    }

    public CompetitionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public int getFirstSeed() {
        return firstSeed;
    }

    public LocalDateTime getRegisterStartAt() {
        return registerStartAt;
    }

    public LocalDateTime getRegisterEndAt() {
        return registerEndAt;
    }

    public LocalDateTime getCompetitionStartAt() {
        return competitionStartAt;
    }

    public LocalDateTime getCompetitionEndAt() {
        return competitionEndAt;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getCurrentRegisters() {
        return currentRegisters;
    }
}
