package io.antcamp.competitionservice.domain.event;

import java.util.UUID;

/**
 * 대회 신청 취소 이벤트.
 * 참가자가 대회 신청을 취소할 때 발행되며, 자산 서비스가 컨슘하여
 * 해당 유저의 대회 전용 계좌를 정리한다.
 */
public record CompetitionCancelledEvent(
        UUID competitionId,
        UUID userId
) {
}
