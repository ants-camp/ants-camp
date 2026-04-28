package io.antcamp.competitionservice.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 대회 종료 이벤트 페이로드.
 * 자산 서비스가 컨슘하여 참가자별 최종 총자산을 계산하고,
 * 결과를 ParticipantsValuatedPayload 이벤트로 랭킹 서비스에 전달한다.
 */
public record CompetitionEndedPayload(
        UUID competitionId,
        List<UUID> participantUserIds,
        LocalDateTime endedAt
) {
}
