package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.domain.model.AccountType;
import io.antcamp.assetservice.domain.event.payload.CompetitionRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionEventConsumer {

    private final AccountService accountService;

    @Transactional
    @KafkaListener(
            topics = "${topics.competition.registered}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "competitionRegisteredFactory"
    )
    public void handleCompetitionRegistered(CompetitionRegisteredEvent payload) {
        log.info("[Kafka] CompetitionRegisteredEvent 수신. userId={}, competitionId={}", payload.userId(), payload.competitionId());
        AccountType accountType = AccountType.valueOf(payload.competitionType().toUpperCase());

        CreateAccountCommand command = new CreateAccountCommand(
                payload.userId(),
                accountType,
                (long) payload.firstSeed(),
                payload.competitionId(),
                payload.competitionName()
        );
        log.info("[Kafka] 대회 등록 계좌 생성 완료. userId={}, competitionId={}", payload.userId(), payload.competitionId());
        accountService.createAccount(command);
    }
}