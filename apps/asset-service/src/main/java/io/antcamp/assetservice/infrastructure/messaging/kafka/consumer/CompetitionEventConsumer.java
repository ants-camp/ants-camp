package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.domain.model.AccountType;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.CompetitionEndedEvent;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.CompetitionRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionEventConsumer {

    private final AccountService accountService;

    @KafkaListener(
            topics = "${topics.competition.finished}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleCompetitionEnded(CompetitionEndedEvent payload) {

        String lockKey = "lock:competition:ended:" + payload.competitionId();
        String lockToken = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, 10, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(acquired)) {
            log.warn("이미 처리 중인 대회 종료 이벤트입니다. competitionId={}", payload.competitionId());
            return;
        }

        try {
            // 기존 로직
        } finally {
            String luaScript =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            "return redis.call('del', KEYS[1]) " +
                            "else return 0 end";

            redisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, Long.class),
                    List.of(lockKey),
                    lockToken
            );
        }
    }
}