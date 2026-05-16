package io.antcamp.rankingservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.rankingservice.application.RankingService;
import io.antcamp.rankingservice.application.event.RankingEventConsumer;
import io.antcamp.rankingservice.domain.event.TotalAssetCalculatedEvent;
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
     * 대회 종료 후 최종 총자산 수신 → DB에 최종 순위 확정 후 Redis 동기화
     */
    @Override
    @KafkaListener(
            topics = "${topics.asset.total-calculated}",
            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
    )
    public void handleTotalAssetCalcuated(TotalAssetCalculatedEvent payload) {
        log.info("[Kafka] TotalAssetCalculatedEvent 수신. competitionId={}, participantCount={}",
                payload.competitionId(), payload.totalAssets().size());
        rankingService.finalizeRankingsWithValuations(payload.competitionId(), payload.totalAssets());
    }
}
