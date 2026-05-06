package io.antcamp.tradeservice.infrastructure.event.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventProducer {

    private final KafkaTemplate<String, TradeSucceededEvent> kafkaTemplate;

    @Value("${spring.kafka.producer.topics.trade-succeeded-info}")
    private String topic;

    public void publishTradeResult(TradeSucceededEvent event) {
        kafkaTemplate.send(topic, event.accountId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[TRADE-SERVICE] Kafka 발행 실패 - accountId:{}, error:{}", event.accountId(), ex.getMessage());
                    } else {
                        log.info("[TRADE-SERVICE] Kafka 발행 성공 - topic:{}, accountId:{}", topic, event.accountId());
                    }
                });
    }
}
