package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.service.RankingService;
import io.antcamp.assetservice.domain.event.payload.CompetitionTicked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionTickedEventConsumer {

    private final RankingService rankingService;

    @KafkaListener(
            topics = "${topics.competition.ticked}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "competitionTickedFactory"
    )
    public void handleCompetitionTicked(CompetitionTicked payload) {
        log.info("[Kafka] CompetitionTickedEvent 수신. competitionId={}", payload.competitionId());
        rankingService.updateRanking(payload.competitionId());
    }
}