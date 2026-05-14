package io.antcamp.competitionservice.infrastructure.messaging.kafka.producer;

import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionAbortedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionCancelledEvent;
import io.antcamp.competitionservice.domain.event.CompetitionEndedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredEvent;
import io.antcamp.competitionservice.domain.event.CompetitionTicked;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Kafka가 없는 환경(로컬 개발 등)에서 사용하는 no-op 구현체.
 * KafkaTemplate 기반의 실제 구현체가 없을 때만 활성화된다.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(CompetitionEventProducer.class)
public class NoOpCompetitionEventProducer implements CompetitionEventProducer {

    @Override
    public void publishCompetitionRegistered(CompetitionRegisteredEvent event) {
        log.info("[NoOp] publishCompetitionRegistered skipped. competitionId={}", event.competitionId());
    }

    @Override
    public void publishCompetitionEnded(CompetitionEndedEvent event) {
        log.info("[NoOp] publishCompetitionEnded skipped. competitionId={}", event.competitionId());
    }

    @Override
    public void publishCompetitionCancelled(CompetitionCancelledEvent event) {
        log.info("[NoOp] publishCompetitionCancelled skipped. competitionId={}", event.competitionId());
    }

    @Override
    public void publishCompetitionAborted(CompetitionAbortedEvent event) {
        log.info("[NoOp] publishCompetitionAborted skipped. competitionId={}", event.competitionId());
    }

    @Override
    public void publishCompetitionTicked(CompetitionTicked event) {
        log.debug("[NoOp] publishCompetitionTicked skipped. competitionId={}", event.competitionId());
    }
}
