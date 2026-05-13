package io.antcamp.rankingservice.application.event;

import io.antcamp.rankingservice.domain.event.TotalAssetCalculatedEvent.ParticipantTotalAsset;
import java.util.List;
import java.util.UUID;

/**
 * 최종 순위가 DB에 저장 완료됐을 때 발행하는 스프링 내부 이벤트. DB 커밋 이후 Redis를 최종값으로 동기화하기 위해 사용한다.
 */
public record RankingFinalizedEvent(
        UUID competitionId,
        List<ParticipantTotalAsset> valuations
) {
}
