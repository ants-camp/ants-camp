package io.antcamp.rankingservice.application.event;

import io.antcamp.rankingservice.domain.event.TotalAssetCalculatedEvent;

public interface RankingEventConsumer {
    /**
     * 대회 종료 후 최종 총자산 수신 → Redis + DB에 최종 순위 저장
     */
    void handleTotalAssetCalcuated(TotalAssetCalculatedEvent payload);
}
