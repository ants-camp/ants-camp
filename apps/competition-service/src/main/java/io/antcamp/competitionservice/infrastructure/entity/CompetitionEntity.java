package io.antcamp.competitionservice.infrastructure.entity;

import common.entity.BaseEntity;
import common.exception.BusinessException;
import common.exception.ErrorCode;
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
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "p_competitions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at is NULL")
public class CompetitionEntity extends BaseEntity implements Persistable<UUID> {

    @Transient
    private boolean isNew = true;

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

    /**
     * 빌더로 모든 필드를 한 번에 받아 객체를 생성한다. 빌더 호출 후 validate()로 누락된 필수값을 즉시 검증한다.
     */
    @Builder(access = AccessLevel.PRIVATE)
    private CompetitionEntity(
            UUID competitionId,
            String name,
            CompetitionType type,
            CompetitionStatus status,
            String description,
            int firstSeed,
            boolean isReadable,
            LocalDateTime registerStartAt,
            LocalDateTime registerEndAt,
            LocalDateTime competitionStartAt,
            LocalDateTime competitionEndAt,
            int minParticipants,
            int maxParticipants,
            int currentRegisters
    ) {
        this.competitionId = competitionId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.description = description;
        this.firstSeed = firstSeed;
        this.isReadable = isReadable;
        this.registerStartAt = registerStartAt;
        this.registerEndAt = registerEndAt;
        this.competitionStartAt = competitionStartAt;
        this.competitionEndAt = competitionEndAt;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.currentRegisters = currentRegisters;
        validate();
    }

    /**
     * 도메인 객체로부터 엔티티를 생성한다.
     */
    public static CompetitionEntity from(Competition domain) {
        return CompetitionEntity.builder()
                .competitionId(domain.getCompetitionId())
                .name(domain.getName())
                .type(domain.getType())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .firstSeed(domain.getFirstSeed())
                .isReadable(domain.isReadable())
                .registerStartAt(domain.getRegisterPeriod().getStartAt())
                .registerEndAt(domain.getRegisterPeriod().getEndAt())
                .competitionStartAt(domain.getCompetitionPeriod().getStartAt())
                .competitionEndAt(domain.getCompetitionPeriod().getEndAt())
                .minParticipants(domain.getParticipantCount().getMin())
                .maxParticipants(domain.getParticipantCount().getMax())
                .currentRegisters(domain.getParticipantCount().getCurrent())
                .build();
    }

    public Competition toDomain() {
        return Competition.reconstitute(
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

    /**
     * 영속 상태에서 도메인 변경분을 반영한다. 식별자(competitionId)는 변경하지 않는다.
     */
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

    /**
     * 빌더 방식의 단점(필수값 누락 시에도 객체가 생성되는 문제)을 보완하기 위해 생성 시점에 필수 참조 타입 값들의 null 여부를 검증한다.
     */
    private void validate() {
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (type == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (description == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (registerStartAt == null || registerEndAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (competitionStartAt == null || competitionEndAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public UUID getId() {
        return competitionId;
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
