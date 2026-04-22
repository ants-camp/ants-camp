package io.antcamp.competitionservice.infrastructure;

import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.domain.CompetitionType;
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
 * 대회 JPA 엔티티 (p_competitions 테이블 매핑)
 */
@Entity
@Table(name = "p_competitions")
public class CompetitionEntity {

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

    @Column(name = "register_start_at", nullable = false)
    private LocalDateTime registerStartAt;

    @Column(name = "register_end_at", nullable = false)
    private LocalDateTime registerEndAt;

    @Column(name = "competition_start_at", nullable = false)
    private LocalDateTime competitionStartAt;

    @Column(name = "competition_end_at", nullable = false)
    private LocalDateTime competitionEndAt;

    @Column(name = "min_participants", nullable = false)
    private int minParticipants = 10;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants = 1000;

    @Column(name = "current_registers", nullable = false)
    private int currentRegisters = 0;

    protected CompetitionEntity() {
    }

    // ─── 정적 팩토리 메서드 ────────────────────────────────────────────────

    /**
     * 도메인 → 엔티티 변환
     */
    public static CompetitionEntity from(Competition domain) {
        CompetitionEntity entity = new CompetitionEntity();
        entity.competitionId = domain.getCompetitionId();
        entity.name = domain.getName();
        entity.type = domain.getType();
        entity.status = domain.getStatus();
        entity.description = domain.getDescription();
        entity.firstSeed = domain.getFirstSeed();
        entity.registerStartAt = domain.getRegisterStartAt();
        entity.registerEndAt = domain.getRegisterEndAt();
        entity.competitionStartAt = domain.getCompetitionStartAt();
        entity.competitionEndAt = domain.getCompetitionEndAt();
        entity.minParticipants = domain.getMinParticipants();
        entity.maxParticipants = domain.getMaxParticipants();
        entity.currentRegisters = domain.getCurrentRegisters();
        return entity;
    }

    /**
     * 엔티티 → 도메인 변환
     */
    public Competition toDomain() {
        return new Competition(
                competitionId,
                name,
                type,
                status,
                description,
                firstSeed,
                registerStartAt,
                registerEndAt,
                competitionStartAt,
                competitionEndAt,
                minParticipants,
                maxParticipants,
                currentRegisters
        );
    }

    // ─── Getters ────────────────────────────────────────────────────────

    public UUID getCompetitionId() {
        return competitionId;
    }

    public String getName() {
        return name;
    }

    public CompetitionType getType() {
        return type;
    }

    public CompetitionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public int getFirstSeed() {
        return firstSeed;
    }

    public LocalDateTime getRegisterStartAt() {
        return registerStartAt;
    }

    public LocalDateTime getRegisterEndAt() {
        return registerEndAt;
    }

    public LocalDateTime getCompetitionStartAt() {
        return competitionStartAt;
    }

    public LocalDateTime getCompetitionEndAt() {
        return competitionEndAt;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getCurrentRegisters() {
        return currentRegisters;
    }
}
