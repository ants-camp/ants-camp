package io.antcamp.competitionservice.presentation.dto.request;

import io.antcamp.competitionservice.domain.model.CompetitionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record CreateCompetitionRequest(
        @NotBlank String name,
        @NotNull CompetitionType type,
        @NotBlank String description,
        @Positive int firstSeed,
        @NotNull LocalDateTime registerStartAt,
        @NotNull LocalDateTime registerEndAt,
        @NotNull LocalDateTime competitionStartAt,
        @NotNull LocalDateTime competitionEndAt,
        @Positive int minParticipants,
        @Positive int maxParticipants
) {
}
