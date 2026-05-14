package io.antcamp.competitionservice.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateCompetitionRequest(

        @NotBlank(message = "대회명은 필수입니다.")
        String name,

        @NotBlank(message = "대회 설명은 필수입니다.")
        String description,

        @NotNull(message = "신청 시작일은 필수입니다.")
        LocalDateTime registerStartAt,

        @NotNull(message = "신청 종료일은 필수입니다.")
        LocalDateTime registerEndAt,

        @NotNull(message = "대회 시작일은 필수입니다.")
        LocalDateTime competitionStartAt,

        @NotNull(message = "대회 종료일은 필수입니다.")
        LocalDateTime competitionEndAt,

        @Min(value = 1, message = "최소 참가 인원은 1명 이상이어야 합니다.")
        int minParticipants,

        @Min(value = 1, message = "최대 참가 인원은 1명 이상이어야 합니다.")
        int maxParticipants,

        // isReadable = true일 때만 필요 (nullable)
        String beforeContents,
        String afterContents,
        String reason
) {
}
