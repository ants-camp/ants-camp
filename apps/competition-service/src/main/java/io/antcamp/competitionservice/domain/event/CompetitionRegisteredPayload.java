package io.antcamp.competitionservice.domain.event;

import java.util.UUID;

/**
 * 대회 신청 이벤트 페이로드.
 * 참가자가 대회에 신청할 때 발행되며, 자산 서비스가 컨슘하여
 * 해당 유저에게 대회 전용 계좌를 생성하고 시드머니를 설정한다.
 */
public record CompetitionRegisteredPayload(
        UUID competitionId,
        String competitionName,
        String competitionType, //PERSONAL, COMPETITION
        int firstSeed,
        UUID userId
) {
}
