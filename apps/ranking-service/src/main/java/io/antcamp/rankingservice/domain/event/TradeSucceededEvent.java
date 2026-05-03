package io.antcamp.rankingservice.domain.event;

import java.util.UUID;

/**
 * 매매 서비스에서 매매 체결 성공 후 발행하는 이벤트.
 * 랭킹 서비스가 컨슘하여 Redis 실시간 순위를 갱신한다 (DB 저장 없음).
 */
public record TradeSucceededEvent(
        UUID userId,
        UUID competitionId,
        Double totalAsset  // 현금 + 보유주식 시가 합산 (소수점 포함)
) {
}
