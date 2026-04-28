package io.antcamp.rankingservice.infrastructure.entity;

import common.entity.BaseEntity;
import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.rankingservice.domain.model.Ranking;
import io.antcamp.rankingservice.domain.model.RankTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_rankings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"competition_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at is NULL")
public class RankingEntity extends BaseEntity implements Persistable<UUID> {

    @Transient
    private boolean isNew = true;

    @Id
    @Column(name = "ranking_id", nullable = false, updatable = false)
    private UUID rankingId;

    @Column(name = "competition_id", nullable = false)
    private UUID competitionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rank")
    private RankTier rank;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "is_finalized", nullable = false)
    private boolean isFinalized;

    @Builder(access = AccessLevel.PRIVATE)
    private RankingEntity(UUID rankingId, UUID competitionId, UUID userId,
                          RankTier rank, LocalDateTime lastUpdatedAt, boolean isFinalized) {
        this.rankingId = rankingId;
        this.competitionId = competitionId;
        this.userId = userId;
        this.rank = rank;
        this.lastUpdatedAt = lastUpdatedAt;
        this.isFinalized = isFinalized;
        validate();
    }

    public static RankingEntity from(Ranking domain) {
        return RankingEntity.builder()
                .rankingId(domain.getRankingId())
                .competitionId(domain.getCompetitionId())
                .userId(domain.getUserId())
                .rank(domain.getRank())
                .lastUpdatedAt(domain.getLastUpdatedAt())
                .isFinalized(domain.isFinalized())
                .build();
    }

    public Ranking toDomain() {
        return Ranking.from(rankingId, competitionId, userId, rank, lastUpdatedAt, isFinalized);
    }

    public void update(Ranking domain) {
        this.rank = domain.getRank();
        this.lastUpdatedAt = domain.getLastUpdatedAt();
        this.isFinalized = domain.isFinalized();
    }

    private void validate() {
        if (rankingId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public UUID getId() {
        return rankingId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}
