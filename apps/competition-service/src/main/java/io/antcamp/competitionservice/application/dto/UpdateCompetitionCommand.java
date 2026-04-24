package io.antcamp.competitionservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateCompetitionCommand(
        UUID competitionId,
        String name,
        String description,
        LocalDateTime registerStartAt,
        LocalDateTime registerEndAt,
        LocalDateTime competitionStartAt,
        LocalDateTime competitionEndAt,
        int minParticipants,
        int maxParticipants,

        // isReadable = true일 때만 필요
        String beforeContents,
        String afterContents,
        String reason,
        String updatedBy
) {
}
