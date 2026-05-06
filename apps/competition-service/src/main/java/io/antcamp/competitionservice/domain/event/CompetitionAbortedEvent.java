package io.antcamp.competitionservice.domain.event;

import java.util.List;
import java.util.UUID;

/**
 * 대회 자체 취소 이벤트.
 * 대회가 CANCELED 상태로 전환될 때 발행되며, 참가자 계좌 정리 등
 * 후속 처리가 필요한 서비스가 각자 컨슘하여 사용한다.
 */
public record CompetitionAbortedEvent(
        UUID competitionId,
        List<UUID> participantUserIds
) {
}
