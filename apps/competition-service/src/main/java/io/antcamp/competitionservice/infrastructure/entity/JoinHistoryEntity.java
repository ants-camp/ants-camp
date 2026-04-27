package io.antcamp.competitionservice.infrastructure.entity;

import common.entity.BaseEntity;
import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.domain.model.JoinHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

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
public class JoinHistoryEntity extends BaseEntity implements Persistable<UUID> {

    @Transient
    private boolean isNew = true;

    @Id
    @Column(name = "join_history_id", nullable = false, updatable = false)
    private UUID joinHistoryId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "competition_id", nullable = false)
    private UUID competitionId;

    @Builder(access = AccessLevel.PRIVATE)
    private JoinHistoryEntity(
            UUID joinHistoryId,
            UUID userId,
            String nickname,
            UUID competitionId
    ) {
        this.joinHistoryId = joinHistoryId;
        this.userId = userId;
        this.nickname = nickname;
        this.competitionId = competitionId;
        validate();
    }

    public static JoinHistoryEntity from(JoinHistory domain) {
        return JoinHistoryEntity.builder()
                .joinHistoryId(domain.getJoinHistoryId())
                .userId(domain.getUserId())
                .nickname(domain.getNickname())
                .competitionId(domain.getCompetitionId())
                .build();
    }

    public JoinHistory toDomain() {
        return JoinHistory.from(
                joinHistoryId,
                userId,
                nickname,
                competitionId
        );
    }

    private void validate() {
        if (joinHistoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public UUID getId() {
        return joinHistoryId;
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
