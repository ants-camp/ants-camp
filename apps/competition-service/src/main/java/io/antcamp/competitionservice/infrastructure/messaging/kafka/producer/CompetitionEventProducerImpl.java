package io.antcamp.competitionservice.infrastructure.messaging.kafka.producer;

import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.payload.CompetitionStartedPayload;
import io.antcamp.competitionservice.infrastructure.messaging.kafka.CompetitionTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(CompetitionTopicProperties.class)
public class CompetitionEventProducerImpl implements CompetitionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CompetitionTopicProperties topicProperties;


    // 대회생성이벤트 -> 계좌 서비스
    @Override
    public void publishCompetitionStarted(CompetitionStartedPayload payload) {
        // key를 competitionId로 두면 같은 대회의 이벤트가 같은 파티션으로 라우팅되어 순서 보장됨
        String key = payload.competitionId().toString();
        String topic = topicProperties.started();

        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] CompetitionStartedEvent 발행 실패. competitionId={}, topic={}",
                                key, topic, ex);
                    } else {
                        log.info(
                                "[Kafka] CompetitionStartedEvent 발행 성공. competitionId={}, topic={}, partition={}, offset={}",
                                key, topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
