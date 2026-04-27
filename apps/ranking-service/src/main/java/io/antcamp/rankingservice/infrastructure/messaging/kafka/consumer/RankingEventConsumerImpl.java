package io.antcamp.rankingservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.rankingservice.application.RankingService;
import io.antcamp.rankingservice.application.event.RankingEventConsumer;
import io.antcamp.rankingservice.domain.event.AssetUpdatedPayload;
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

    @Override
    @KafkaListener(
            topics = "${topics.ranking.asset-updated}",
            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
    )
    public void handleAssetUpdated(AssetUpdatedPayload payload) {
        log.info("[Kafka] AssetUpdatedEvent 수신. userId={}, competitionId={}, totalAsset={}",
                payload.userId(), payload.competitionId(), payload.totalAsset());
        rankingService.upsertRanking(
                payload.competitionId(),
                payload.userId(),
                payload.totalAsset()
        );
    }
}
