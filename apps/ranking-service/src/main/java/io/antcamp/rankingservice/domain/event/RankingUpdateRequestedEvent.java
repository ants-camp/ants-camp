package io.antcamp.rankingservice.domain.event;

import java.util.List;
import java.util.UUID;

/**
 * 매매 서비스가 1분마다 대회 참가자 전체의 총자산을 계산한 뒤 발행하는 이벤트.
 * 랭킹 서비스가 컨슘하여 Redis sorted set을 일괄 갱신한다.
 * (매매 미체결 상태에서도 보유주식 시가 변동을 랭킹에 반영하기 위함)
 */
public record RankingUpdateRequestedEvent(
        UUID competitionId,
        List<ParticipantAsset> participants
) {
    public record ParticipantAsset(
            UUID userId,
            Double totalAsset  // 현금 + 보유주식 시가 합산 (소수점 포함)
    ) {
    }
}
