package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.domain.event.payload.CompetitionCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionCancelledEventConsumer {

    private final AccountService accountService;

    @Transactional
    @KafkaListener(
            topics = "${topics.competition.cancelled}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "competitionCancelledFactory"
    )
    public void handleCompetitionCancelled(CompetitionCancelledEvent payload) {
        log.info("[Kafka] CompetitionCancelledEvent 수신. competitionId={}, userId={}",
                payload.competitionId(), payload.userId());

        accountService.deleteByUserIdAndCompetitionId(payload.userId(), payload.competitionId());

        log.info("[Kafka] 신청 취소 계좌 softDelete 완료. competitionId={}, userId={}",
                payload.competitionId(), payload.userId());
    }
}