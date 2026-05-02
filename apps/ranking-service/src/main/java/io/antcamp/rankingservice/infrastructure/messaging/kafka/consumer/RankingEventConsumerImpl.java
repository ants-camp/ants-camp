package io.antcamp.rankingservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.rankingservice.application.RankingService;
import io.antcamp.rankingservice.application.event.RankingEventConsumer;
import io.antcamp.rankingservice.domain.event.TotalAssetCalcuatedEvent;
import io.antcamp.rankingservice.domain.event.TradeSucceededEvent;
import io.antcamp.rankingservice.infrastructure.messaging.kafka.RankingTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(RankingTopicProperties.class)
public class RankingEventConsumerImpl implements RankingEventConsumer {

    private final RankingService rankingService;

    /**
     * 매매 체결마다 호출 → Redis만 갱신 (실시간 순위, DB 저장 없음)
     */
    @Override
    @KafkaListener(
            topics = "${topics.trade.trade-succeeded}",
            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
    )
    public void handleTradeSucceeded(TradeSucceededEvent payload) {
        log.info("[Kafka] TradeSucceededEvent 수신. userId={}, competitionId={}, totalAsset={}",
                payload.userId(), payload.competitionId(), payload.totalAsset());
        rankingService.upsertRanking(
                payload.competitionId(),
                payload.userId(),
                payload.totalAsset()
        );
    }

//    /**
//     * 1분마다 수신 → 대회 참가자 전체 총자산을 Redis에 일괄 갱신 (매매 미체결 시 시가 변동 반영)
//     */
//    @Override
//    @KafkaListener(
//            topics = "${topics.trade.ranking-update-requested}",
//            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
//    )
//    public void handleRankingUpdateRequested(RankingUpdateRequestedEvent payload) {
//        log.info("[Kafka] RankingUpdateRequestedEvent 수신. competitionId={}, participantCount={}",
//                payload.competitionId(), payload.participants().size());
//        rankingService.batchUpsertRankings(payload.competitionId(), payload.participants());
//    }

    /**
     * 대회 종료 후 최종 총자산 수신 → Redis + DB에 최종 순위 확정
     */
    @Override
    @KafkaListener(
            topics = "${topics.asset.total-calculated}",
            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
    )
    public void handleTotalAssetCalcuated(TotalAssetCalcuatedEvent payload) {
        log.info("[Kafka] TotalAssetCalcuatedEvent 수신. competitionId={}, participantCount={}",
                payload.competitionId(), payload.totalAssets().size());
        rankingService.finalizeRankingsWithValuations(payload.competitionId(), payload.totalAssets());
    }
}
