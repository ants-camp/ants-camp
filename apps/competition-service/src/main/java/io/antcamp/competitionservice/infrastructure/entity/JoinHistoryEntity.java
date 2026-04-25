package io.antcamp.competitionservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.competitionservice.domain.model.JoinHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 대회 참여자 명부 JPA 엔티티 (p_join_historys 테이블 매핑)
 */
@Entity
@Table(
        name = "p_join_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_join_history_user_competition",
                        columnNames = {"user_id", "competition_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at is NULL")
public class JoinHistoryEntity extends BaseEntity {

    @Id
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID joinhistoryId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "competition_id", nullable = false)
    private UUID competitionId; // 대회 엔티티의 pk ( p_competitions.competition_id 참조 )

    // ─── 정적 팩토리 메서드 ────────────────────────────────────────────────

    public static JoinHistoryEntity from(JoinHistory domain) {
        JoinHistoryEntity entity = new JoinHistoryEntity();
        entity.joinhistoryId = domain.getJoinhistoryId();
        entity.userId = domain.getUserId();
        entity.nickname = domain.getNickname();
        entity.competitionId = domain.getCompetitionId();
        return entity;
    }

    public JoinHistory toDomain() {
        return JoinHistory.from(
                joinhistoryId,
                userId,
                nickname,
                competitionId
        );
    }
}
