package io.antcamp.competitionservice.infrastructure;

import io.antcamp.competitionservice.domain.JoinHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

/**
 * 대회 참여자 명부 JPA 엔티티 (p_join_historys 테이블 매핑)
 */
@Entity
@Table(
        name = "p_join_historys",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_join_history_user_competition",
                        columnNames = {"user_id", "competition_id"}
                )
        }
)
public class JoinHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID participantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "competition_id", nullable = false)
    private UUID competitionId;

    protected JoinHistoryEntity() {
    }

    // ─── 정적 팩토리 메서드 ────────────────────────────────────────────────

    /**
     * 도메인 → 엔티티 변환
     */
    public static JoinHistoryEntity from(JoinHistory domain) {
        JoinHistoryEntity entity = new JoinHistoryEntity();
        entity.participantId = domain.getParticipantId();
        entity.userId = domain.getUserId();
        entity.nickname = domain.getNickname();
        entity.competitionId = domain.getCompetitionId();
        return entity;
    }

    /**
     * 엔티티 → 도메인 변환
     */
    public JoinHistory toDomain() {
        return new JoinHistory(
                participantId,
                userId,
                nickname,
                competitionId
        );
    }

    // ─── Getters ────────────────────────────────────────────────────────

    public UUID getParticipantId() {
        return participantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public UUID getCompetitionId() {
        return competitionId;
    }
}
