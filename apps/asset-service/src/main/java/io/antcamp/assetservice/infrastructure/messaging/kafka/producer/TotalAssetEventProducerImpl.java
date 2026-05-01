package io.antcamp.assetservice.infrastructure.messaging.kafka.producer;

import io.antcamp.assetservice.domain.repository.TotalAssetEventProducer;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.TotalAssetCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TotalAssetEventProducerImpl implements TotalAssetEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.asset.total-calculated}")
    private String totalAssetCalculatedTopic;

    @Override
    public void sendTotalAssetCalculated(TotalAssetCalculatedEvent event) {
        kafkaTemplate.send(totalAssetCalculatedTopic, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("총 자산 이벤트 발행 실패: {}", ex.getMessage());
                    } else {
                        log.info("총 자산 이벤트 발행 성공: competitionId={}", event.competitionId());
                    }
                });
    }
}