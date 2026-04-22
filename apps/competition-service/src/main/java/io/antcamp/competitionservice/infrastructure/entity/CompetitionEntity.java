package io.antcamp.competitionservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.domain.CompetitionType;
import io.antcamp.competitionservice.domain.vo.CompetitionPeriod;
import io.antcamp.competitionservice.domain.vo.ParticipantCount;
import io.antcamp.competitionservice.domain.vo.RegisterPeriod;
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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 대회 JPA 엔티티 (p_competitions 테이블 매핑) VO 필드는 @Embedded 없이 직접 flat하게 매핑 → 도메인 VO에 JPA 의존성 없음
 */
@Entity
@Table(name = "p_competitions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at is NULL")
public class CompetitionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    // RegisterPeriod VO 필드 flat 매핑
    @Column(name = "register_start_at", nullable = false)
    private LocalDateTime registerStartAt;

    @Column(name = "register_end_at", nullable = false)
    private LocalDateTime registerEndAt;

    // CompetitionPeriod VO 필드 flat 매핑
    @Column(name = "competition_start_at", nullable = false)
    private LocalDateTime competitionStartAt;

    @Column(name = "competition_end_at", nullable = false)
    private LocalDateTime competitionEndAt;

    // ParticipantCount VO 필드 flat 매핑
    @Column(name = "min_participants", nullable = false)
    private int minParticipants;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_registers", nullable = false)
    private int currentRegisters;

    // ─── 정적 팩토리 메서드 ────────────────────────────────────────────────

    public static CompetitionEntity from(Competition domain) {
        CompetitionEntity entity = new CompetitionEntity();
        entity.competitionId = domain.getCompetitionId();
        entity.name = domain.getName();
        entity.type = domain.getType();
        entity.status = domain.getStatus();
        entity.description = domain.getDescription();
        entity.firstSeed = domain.getFirstSeed();
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
        return Competition.restore()
                .competitionId(competitionId)
                .name(name)
                .type(type)
                .status(status)
                .description(description)
                .firstSeed(firstSeed)
                .registerPeriod(new RegisterPeriod(registerStartAt, registerEndAt))
                .competitionPeriod(new CompetitionPeriod(competitionStartAt, competitionEndAt))
                .participantCount(new ParticipantCount(minParticipants, maxParticipants, currentRegisters))
                .build();
    }
}
