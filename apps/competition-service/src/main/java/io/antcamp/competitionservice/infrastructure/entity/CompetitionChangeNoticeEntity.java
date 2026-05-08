package io.antcamp.competitionservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "p_competition_change_notices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompetitionChangeNoticeEntity extends BaseEntity implements Persistable<UUID> {

    @Transient
    private boolean isNew = true;  // 추가

    @Id
    @Column(name = "notice_id", nullable = false, updatable = false)
    private UUID noticeId;

    @Column(name = "competition_id", nullable = false)
    private UUID competitionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "competition_status", nullable = false)
    private CompetitionStatus competitionStatus;

    @Column(name = "before_contents", nullable = false, columnDefinition = "TEXT")
    private String beforeContents;

    @Column(name = "after_contents", nullable = false, columnDefinition = "TEXT")
    private String afterContents;

    @Column(name = "reason", nullable = false)
    private String reason;

    public static CompetitionChangeNoticeEntity from(CompetitionChangeNotice domain) {
        CompetitionChangeNoticeEntity entity = new CompetitionChangeNoticeEntity();
        entity.noticeId = domain.getNoticeId();
        entity.competitionId = domain.getCompetitionId();
        entity.competitionStatus = domain.getCompetitionStatus();
        entity.beforeContents = domain.getBeforeContents();
        entity.afterContents = domain.getAfterContents();
        entity.reason = domain.getReason();
        return entity;
    }

    public CompetitionChangeNotice toDomain() {
        return CompetitionChangeNotice.reconstitute(
                noticeId,
                competitionId,
                competitionStatus,
                beforeContents,
                afterContents,
                reason,
                getCreatedAt()  // BaseEntity에서 가져옴
        );
    }

    @Override
    public UUID getId() {
        return noticeId;  // competitionId 말고 noticeId
    }

    @Override
    public boolean isNew() {
        return isNew;  // false 말고 isNew 필드 반환
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;  // DB에서 조회하거나 저장 후엔 false로
    }
}
