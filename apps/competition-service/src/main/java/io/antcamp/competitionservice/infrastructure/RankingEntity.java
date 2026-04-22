package io.antcamp.competitionservice.infrastructure;

import io.antcamp.competitionservice.domain.RankTier;
import io.antcamp.competitionservice.domain.Ranking;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 랭킹 JPA 엔티티 (p_rankings 테이블 매핑)
 */
@Entity
@Table(name = "p_rankings")
public class RankingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ranking_id", nullable = false, updatable = false)
    private UUID rankingId;

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(name = "competition_id", nullable = false)
    private UUID competitionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rank", nullable = false)
    private RankTier rank;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "is_finalized", nullable = false)
    private boolean isFinalized = false;

    protected RankingEntity() {
    }

    // ─── 정적 팩토리 메서드 ────────────────────────────────────────────────

    /**
     * 도메인 → 엔티티 변환
     */
    public static RankingEntity from(Ranking domain) {
        RankingEntity entity = new RankingEntity();
        entity.rankingId = domain.getRankingId();
        entity.participantId = domain.getParticipantId();
        entity.competitionId = domain.getCompetitionId();
        entity.rank = domain.getRank();
        entity.lastUpdatedAt = domain.getLastUpdatedAt();
        entity.isFinalized = domain.isFinalized();
        return entity;
    }

    /**
     * 엔티티 → 도메인 변환
     */
    public Ranking toDomain() {
        return new Ranking(
                rankingId,
                participantId,
                competitionId,
                rank,
                lastUpdatedAt,
                isFinalized
        );
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
