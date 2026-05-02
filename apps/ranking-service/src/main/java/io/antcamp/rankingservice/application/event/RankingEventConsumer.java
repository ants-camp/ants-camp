package io.antcamp.rankingservice.application.event;

import io.antcamp.rankingservice.domain.event.TotalAssetCalcuatedEvent;
import io.antcamp.rankingservice.domain.event.TradeSucceededEvent;

public interface RankingEventConsumer {
    /**
     * 매매 체결 시 단건 실시간 순위 갱신 (Redis only)
     */
    void handleTradeSucceeded(TradeSucceededEvent payload);

//    /**
//     * 1분마다 대회 참가자 전체 총자산 수신 → Redis 일괄 갱신 (매매 미체결 시 시가 변동 반영)
//     */
//    void handleRankingUpdateRequested(RankingUpdateRequestedEvent payload);

    /**
     * 대회 종료 후 최종 총자산 수신 → Redis + DB에 최종 순위 저장
     */
    void handleTotalAssetCalcuated(TotalAssetCalcuatedEvent payload);
}
