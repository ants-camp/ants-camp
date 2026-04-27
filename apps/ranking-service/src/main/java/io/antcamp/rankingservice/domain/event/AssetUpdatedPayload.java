package io.antcamp.rankingservice.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 자산 서비스에서 매매 체결 후 평가금 갱신 시 발행하는 이벤트 payload. competitionId 포함 여부는 자산팀과 협의 후 확정.
 */
public record AssetUpdatedPayload(
        UUID userId,
        UUID competitionId,
        BigDecimal totalAsset,
        LocalDateTime occurredAt
) {
}
