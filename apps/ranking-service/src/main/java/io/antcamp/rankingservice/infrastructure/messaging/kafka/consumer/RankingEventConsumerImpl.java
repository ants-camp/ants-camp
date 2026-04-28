package io.antcamp.rankingservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.rankingservice.application.RankingService;
import io.antcamp.rankingservice.application.event.RankingEventConsumer;
import io.antcamp.rankingservice.domain.event.AssetUpdatedPayload;
import io.antcamp.rankingservice.domain.event.ParticipantsValuatedPayload;
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

    /** 매매 체결마다 호출 → Redis만 갱신 (실시간 순위, DB 저장 없음) */
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

    /** 대회 종료 후 최종 총자산 수신 → Redis + DB에 최종 순위 확정 */
    @Override
    @KafkaListener(
            topics = "${topics.ranking.participants-valuated}",
            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
    )
    public void handleParticipantsValuated(ParticipantsValuatedPayload payload) {
        log.info("[Kafka] ParticipantsValuatedEvent 수신. competitionId={}, participantCount={}",
                payload.competitionId(), payload.valuations().size());
        rankingService.finalizeRankingsWithValuations(payload.competitionId(), payload.valuations());
    }
}
