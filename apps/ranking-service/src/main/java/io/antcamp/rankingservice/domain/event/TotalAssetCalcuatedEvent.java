package io.antcamp.rankingservice.domain.event;

import java.util.List;
import java.util.UUID;

/**
 * 자산 서비스가 대회 종료 후 참가자별 최종 총자산을 계산한 뒤 발행하는 이벤트.
 * 랭킹 서비스가 컨슘하여 최종 순위를 Redis와 DB에 모두 저장한다.
 */
public record TotalAssetCalcuatedEvent(
        UUID competitionId,
        List<ParticipantTotalAsset> totalAssets
) {
    public record ParticipantTotalAsset(
            UUID userId,
            Double totalAsset  // 현금 + 보유주식 시가 합산 (소수점 포함)
    ) {
    }
}
