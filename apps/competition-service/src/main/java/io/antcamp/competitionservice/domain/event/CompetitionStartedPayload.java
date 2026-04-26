package io.antcamp.competitionservice.domain.event.payload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 대회 시작 이벤트 페이로드. 계좌 서비스가 컨슘하여 참가자별 대회 전용 계좌를 생성하고 초기 시드머니를 설정한다.
 */
public record CompetitionStartedPayload(
        UUID competitionId,
        String competitionName,
        int firstSeed,
        List<Participant> participants,
        LocalDateTime startedAt
) {
    public record Participant(
            UUID userId,
            String nickname
    ) {
    }
}
