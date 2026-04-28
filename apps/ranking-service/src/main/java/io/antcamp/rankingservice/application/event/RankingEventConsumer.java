package io.antcamp.rankingservice.application.event;

import io.antcamp.rankingservice.domain.event.AssetUpdatedPayload;
import io.antcamp.rankingservice.domain.event.ParticipantsValuatedPayload;

public interface RankingEventConsumer {
    /** 매매 체결 시 실시간 순위 갱신 (Redis only) */
    void handleAssetUpdated(AssetUpdatedPayload payload);

    /** 대회 종료 후 최종 총자산 수신 → Redis + DB에 최종 순위 저장 */
    void handleParticipantsValuated(ParticipantsValuatedPayload payload);
}
