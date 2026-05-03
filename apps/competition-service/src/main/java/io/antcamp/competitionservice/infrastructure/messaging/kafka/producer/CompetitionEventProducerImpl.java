package io.antcamp.competitionservice.infrastructure.messaging.kafka.producer;

import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionAbortedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionCancelledEvent;
import io.antcamp.competitionservice.domain.event.CompetitionEndedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredEvent;
import io.antcamp.competitionservice.domain.event.CompetitionTicked;
import io.antcamp.competitionservice.infrastructure.messaging.kafka.CompetitionTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(CompetitionTopicProperties.class)
public class CompetitionEventProducerImpl implements CompetitionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CompetitionTopicProperties topicProperties;

    // registered: 대회 신청 이벤트 → 자산 서비스 (개인별 대회 계좌 생성)
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

    // finished: 대회 종료 이벤트 → 자산 서비스 (최종 총자산 계산 후 랭킹 서비스로 전달)
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

    // cancelled: 대회 신청 취소 이벤트 → 자산 서비스 (대회 전용 계좌 정리)
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

    // aborted: 대회 자체 취소 이벤트 → 참가자 계좌 정리 등 후속 처리가 필요한 서비스가 컨슘
    @Override
    public void publishCompetitionAborted(CompetitionAbortedEvent event) {
        String key = event.competitionId().toString();
        String topic = topicProperties.aborted();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] CompetitionAbortedEvent 발행 실패. competitionId={}, participantCount={}, topic={}",
                                key, event.participantUserIds().size(), topic, ex);
                    } else {
                        log.info("[Kafka] CompetitionAbortedEvent 발행 성공. competitionId={}, participantCount={}, topic={}, partition={}, offset={}",
                                key, event.participantUserIds().size(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    // ticked: 틱 이벤트 → 자산 서비스 (1분마다 총자산 계산 후 Redis 랭킹 반영)
    @Override
    public void publishCompetitionTicked(CompetitionTicked event) {
        String key = event.competitionId().toString();
        String topic = topicProperties.ticked();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] CompetitionTicked 발행 실패. competitionId={}, topic={}",
                                key, topic, ex);
                    } else {
                        log.debug("[Kafka] CompetitionTicked 발행 성공. competitionId={}, topic={}, partition={}, offset={}",
                                key, topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    @Override
    public void publishCompetitionTicked(CompetitionTicked event) {
        String key = event.competitionId().toString();
        String topic = topicProperties.ticked();

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);

        record.headers().add(
                "__TypeId__",
                "io.antcamp.assetservice.domain.event.payload.CompetitionTicked"
                        .getBytes(StandardCharsets.UTF_8)
        );

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] CompetitionTicked 발행 실패. competitionId={}, topic={}",
                                key, topic, ex);
                    } else {
                        log.debug("[Kafka] CompetitionTicked 발행 성공. competitionId={}, topic={}, partition={}, offset={}",
                                key, topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
