package io.antcamp.assetservice.infrastructure.messaging.kafka.producer;

import io.antcamp.assetservice.application.dto.query.ParticipantTotalAssetResult;
import io.antcamp.assetservice.domain.repository.TotalAssetEventProducer;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.TotalAssetCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class TotalAssetEventProducerImpl implements TotalAssetEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.asset.total-calculated}")
    private String totalAssetCalculatedTopic;

    @Override
    public void sendTotalAssetCalculated(UUID competitionId, List<ParticipantTotalAssetResult> totalAssets) {
        List<TotalAssetCalculatedEvent.ParticipantTotalAsset> payload = totalAssets.stream()
                .map(r -> new TotalAssetCalculatedEvent.ParticipantTotalAsset(r.userId(), r.totalAsset()))
                .toList();

        kafkaTemplate.send(totalAssetCalculatedTopic, new TotalAssetCalculatedEvent(competitionId, payload))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("총 자산 이벤트 발행 실패: {}", ex.getMessage());
                    } else {
                        log.info("총 자산 이벤트 발행 성공: competitionId={}", competitionId);
                    }
                });
    }
}