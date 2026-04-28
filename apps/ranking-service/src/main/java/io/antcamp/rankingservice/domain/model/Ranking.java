package io.antcamp.rankingservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Ranking {

    private final UUID rankingId;
    private final UUID competitionId;
    private final UUID userId;
    private RankTier rank;
    private LocalDateTime lastUpdatedAt;
    private boolean isFinalized;

    @Builder(access = AccessLevel.PRIVATE)
    private Ranking(UUID competitionId, UUID userId) {
        this.rankingId = UUID.randomUUID();
        this.competitionId = competitionId;
        this.userId = userId;
        this.rank = null;
        this.lastUpdatedAt = LocalDateTime.now();
        this.isFinalized = false;
        validate();
    }

    private Ranking(UUID rankingId, UUID competitionId, UUID userId,
                    RankTier rank, LocalDateTime lastUpdatedAt, boolean isFinalized) {
        this.rankingId = rankingId;
        this.competitionId = competitionId;
        this.userId = userId;
        this.rank = rank;
        this.lastUpdatedAt = lastUpdatedAt;
        this.isFinalized = isFinalized;
        validate();
    }

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────────

    public static Ranking createRanking(UUID competitionId, UUID userId) {
        return Ranking.builder()
                .competitionId(competitionId)
                .userId(userId)
                .build();
    }

    public static Ranking from(UUID rankingId, UUID competitionId, UUID userId,
                               RankTier rank, LocalDateTime lastUpdatedAt, boolean isFinalized) {
        return new Ranking(rankingId, competitionId, userId, rank, lastUpdatedAt, isFinalized);
    }

    // ─── 도메인 행위 ─────────────────────────────────────────────────────

    public void markUpdated() {
        if (isFinalized) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * 대회 종료 시점에 최종 티어를 확정한다. 확정 후에는 변경 불가.
     */
    public void finalize(RankTier tier) {
        if (isFinalized) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        this.rank = tier;
        this.isFinalized = true;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    private void validate() {
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
