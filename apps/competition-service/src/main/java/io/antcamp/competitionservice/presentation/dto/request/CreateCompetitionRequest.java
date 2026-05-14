package io.antcamp.competitionservice.presentation.dto.request;

import io.antcamp.competitionservice.domain.model.CompetitionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record CreateCompetitionRequest(
        @NotBlank(message = "대회명은 필수입니다.") String name,
        @NotNull(message = "대회 유형은 필수입니다.") CompetitionType type,
        @NotBlank(message = "대회 설명은 필수입니다.") String description,
        @Positive(message = "시드머니는 0보다 커야 합니다.") int firstSeed,
        @NotNull(message = "신청 시작일은 필수입니다.") LocalDateTime registerStartAt,
        @NotNull(message = "신청 종료일은 필수입니다.") LocalDateTime registerEndAt,
        @NotNull(message = "대회 시작일은 필수입니다.") LocalDateTime competitionStartAt,
        @NotNull(message = "대회 종료일은 필수입니다.") LocalDateTime competitionEndAt,
        @Positive(message = "최소 참가 인원은 0보다 커야 합니다.") int minParticipants,
        @Positive(message = "최대 참가 인원은 0보다 커야 합니다.") int maxParticipants
) {
}
