package io.antcamp.competitionservice.infrastructure.messaging.kafka.producer;

import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionCancelledEvent;
import io.antcamp.competitionservice.domain.event.CompetitionEndedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredEvent;
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

    // 대회 신청 이벤트 -> 자산 서비스 (개인별 대회 계좌 생성)
    @Override
    public void publishCompetitionRegistered(CompetitionRegisteredEvent event) {
        String key = event.competitionId().toString();
        String topic = topicProperties.registered();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] CompetitionRegisteredEvent 발행 실패. competitionId={}, userId={}, topic={}",
                                key, event.userId(), topic, ex);
                    } else {
                        log.info("[Kafka] CompetitionRegisteredEvent 발행 성공. competitionId={}, userId={}, topic={}, partition={}, offset={}",
                                key, event.userId(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    // 대회 신청 취소 이벤트 -> 자산 서비스 (대회 전용 계좌 정리)
    @Override
    public void publishCompetitionCancelled(CompetitionCancelledEvent event) {
        String key = event.competitionId().toString();
        String topic = topicProperties.cancelled();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] CompetitionCancelledEvent 발행 실패. competitionId={}, userId={}, topic={}",
                                key, event.userId(), topic, ex);
                    } else {
                        log.info("[Kafka] CompetitionCancelledEvent 발행 성공. competitionId={}, userId={}, topic={}, partition={}, offset={}",
                                key, event.userId(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    // 대회 종료 이벤트 -> 자산 서비스 (최종 총자산 계산 후 랭킹 서비스로 전달)
    @Override
    public void publishCompetitionEnded(CompetitionEndedEvent event) {
        String key = event.competitionId().toString();
        String topic = topicProperties.finished();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] CompetitionEndedEvent 발행 실패. competitionId={}, topic={}",
                                key, topic, ex);
                    } else {
                        log.info("[Kafka] CompetitionEndedEvent 발행 성공. competitionId={}, participantCount={}, topic={}, partition={}, offset={}",
                                key, event.participantUserIds().size(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
