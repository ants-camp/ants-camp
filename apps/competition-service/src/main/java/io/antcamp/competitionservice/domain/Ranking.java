package io.antcamp.competitionservice.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 랭킹 도메인 클래스 (순수 도메인 모델 - JPA 의존성 없음)
 */
public class Ranking {

    private final UUID rankingId;
    private final UUID participantId;
    private final UUID competitionId;
    private RankTier rank;
    private LocalDateTime lastUpdatedAt;
    private boolean isFinalized;

    // 신규 랭킹 생성 생성자
    public Ranking(UUID participantId, UUID competitionId, RankTier rank) {
        this.rankingId = UUID.randomUUID();
        this.participantId = participantId;
        this.competitionId = competitionId;
        this.rank = rank;
        this.lastUpdatedAt = LocalDateTime.now();
        this.isFinalized = false;
    }

    // 재구성용 생성자 (DB 조회 등)
    public Ranking(
            UUID rankingId,
            UUID participantId,
            UUID competitionId,
            RankTier rank,
            LocalDateTime lastUpdatedAt,
            boolean isFinalized
    ) {
        this.rankingId = rankingId;
        this.participantId = participantId;
        this.competitionId = competitionId;
        this.rank = rank;
        this.lastUpdatedAt = lastUpdatedAt;
        this.isFinalized = isFinalized;
    }

    // ─── 도메인 행위 ──────────────────────────────────────────────────────

    /**
     * 랭킹 갱신 (대회 진행 중에만 가능)
     */
    public void updateRank(RankTier newRank) {
        if (this.isFinalized) {
            throw new IllegalStateException("이미 확정된 랭킹은 수정할 수 없습니다.");
        }
        this.rank = newRank;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * 랭킹 최종 확정 (대회 종료 시)
     */
    public void finalizeRank() {
        if (this.isFinalized) {
            throw new IllegalStateException("이미 확정된 랭킹입니다.");
        }
        this.isFinalized = true;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * 마지막 갱신으로부터 경과 시간(분) 조회
     */
    public long getMinutesSinceLastUpdate() {
        if (lastUpdatedAt == null) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(lastUpdatedAt, LocalDateTime.now()).toMinutes();
    }

    // ─── Getters ────────────────────────────────────────────────────────

    public UUID getRankingId() {
        return rankingId;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public UUID getCompetitionId() {
        return competitionId;
    }

    public RankTier getRank() {
        return rank;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public boolean isFinalized() {
        return isFinalized;
    }
}
