package io.antcamp.competitionservice.presentation.dto;

import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindCompetitionChangeNoticeResponse(
        UUID noticeId,
        CompetitionStatus competitionStatus,
        String beforeContents,
        String afterContents,
        String reason,
        LocalDateTime createdAt
) {
    public static FindCompetitionChangeNoticeResponse from(CompetitionChangeNotice notice) {
        return new FindCompetitionChangeNoticeResponse(
                notice.getNoticeId(),
                notice.getCompetitionStatus(),
                notice.getBeforeContents(),
                notice.getAfterContents(),
                notice.getReason(),
                notice.getCreatedAt()
        );
    }
}
