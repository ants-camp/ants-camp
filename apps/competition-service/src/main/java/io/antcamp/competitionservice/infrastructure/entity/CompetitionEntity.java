package io.antcamp.competitionservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionPeriod;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.domain.model.CompetitionType;
import io.antcamp.competitionservice.domain.model.ParticipantCount;
import io.antcamp.competitionservice.domain.model.RegisterPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "p_competitions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at is NULL")
public class CompetitionEntity extends BaseEntity implements Persistable<UUID> {

    @Transient
    private boolean isNew = true;  // 추가

    @Id
    @Column(name = "competition_id", nullable = false, updatable = false)
    private UUID competitionId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CompetitionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompetitionStatus status;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "first_seed", nullable = false)
    private int firstSeed;

    @Column(name = "is_readable", nullable = false)
    private boolean isReadable;

    @Column(name = "register_start_at", nullable = false)
    private LocalDateTime registerStartAt;

    @Column(name = "register_end_at", nullable = false)
    private LocalDateTime registerEndAt;

    @Column(name = "competition_start_at", nullable = false)
    private LocalDateTime competitionStartAt;

    @Column(name = "competition_end_at", nullable = false)
    private LocalDateTime competitionEndAt;

    @Column(name = "min_participants", nullable = false)
    private int minParticipants;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_registers", nullable = false)
    private int currentRegisters;

    public static CompetitionEntity from(Competition domain) {
        CompetitionEntity entity = new CompetitionEntity();
        entity.competitionId = domain.getCompetitionId();
        entity.name = domain.getName();
        entity.type = domain.getType();
        entity.status = domain.getStatus();
        entity.description = domain.getDescription();
        entity.firstSeed = domain.getFirstSeed();
        entity.isReadable = domain.isReadable();
        entity.registerStartAt = domain.getRegisterPeriod().getStartAt();
        entity.registerEndAt = domain.getRegisterPeriod().getEndAt();
        entity.competitionStartAt = domain.getCompetitionPeriod().getStartAt();
        entity.competitionEndAt = domain.getCompetitionPeriod().getEndAt();
        entity.minParticipants = domain.getParticipantCount().getMin();
        entity.maxParticipants = domain.getParticipantCount().getMax();
        entity.currentRegisters = domain.getParticipantCount().getCurrent();
        return entity;
    }

    public Competition toDomain() {
        return Competition.from(
                competitionId,
                name,
                type,
                status,
                description,
                firstSeed,
                isReadable,
                RegisterPeriod.of(registerStartAt, registerEndAt),
                CompetitionPeriod.of(competitionStartAt, competitionEndAt),
                ParticipantCount.of(minParticipants, maxParticipants, currentRegisters)
        );
    }

    public void update(Competition domain) {
        this.name = domain.getName();
        this.type = domain.getType();
        this.status = domain.getStatus();
        this.description = domain.getDescription();
        this.firstSeed = domain.getFirstSeed();
        this.isReadable = domain.isReadable();
        this.registerStartAt = domain.getRegisterPeriod().getStartAt();
        this.registerEndAt = domain.getRegisterPeriod().getEndAt();
        this.competitionStartAt = domain.getCompetitionPeriod().getStartAt();
        this.competitionEndAt = domain.getCompetitionPeriod().getEndAt();
        this.minParticipants = domain.getParticipantCount().getMin();
        this.maxParticipants = domain.getParticipantCount().getMax();
        this.currentRegisters = domain.getParticipantCount().getCurrent();
    }

    @Override
    public UUID getId() {
        return competitionId;  // null 말고 실제 id 반환
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
