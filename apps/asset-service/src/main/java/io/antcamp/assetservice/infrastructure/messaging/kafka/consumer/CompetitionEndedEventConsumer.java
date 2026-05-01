package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.dto.query.ParticipantTotalAssetResult;
import io.antcamp.assetservice.application.service.AssetService;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import io.antcamp.assetservice.application.event.TotalAssetEventProducer;
import io.antcamp.assetservice.infrastructure.client.StockPriceClient;
import io.antcamp.assetservice.domain.event.payload.CompetitionEndedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionEndedEventConsumer {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceClient stockPriceClient;
    private final RedisTemplate<String, Long> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final AssetService assetService;
    private final TotalAssetEventProducer totalAssetEventProducer;

    @KafkaListener(
            topics = "${topics.competition.finished}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleCompetitionEnded(CompetitionEndedEvent payload) {

        String lockKey = "lock:competition:ended:" + payload.competitionId();
        String lockToken = UUID.randomUUID().toString();

        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, 10, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(acquired)) {
            log.warn("이미 처리 중인 대회 종료 이벤트입니다. competitionId={}", payload.competitionId());
            return;
        }

        try {
            String cachePrefix = "stock:price:" + payload.competitionId() + ":";

            List<Account> accounts = accountRepository.findAllByCompetitionId(payload.competitionId());

            if (accounts.isEmpty() || accounts.get(0).isEnded()) {
                log.warn("이미 종료 처리된 대회입니다. competitionId={}", payload.competitionId());
                return;
            }
            //캐싱
            Map<String, Long> priceCache = new HashMap<>();
            for (Account account : accounts) {
                List<Holding> holdings = holdingRepository.findAllByAccountId(account.getAccountId());
                for (Holding holding : holdings) {
                    String cacheKey = cachePrefix + holding.getStockCode();
                    Long price = redisTemplate.opsForValue().get(cacheKey);
                    if (price == null) {
                        price = stockPriceClient.getPriceAt(holding.getStockCode(), payload.endedAt());
                        redisTemplate.opsForValue().set(cacheKey, price, 1, TimeUnit.HOURS);
                    }
                    priceCache.put(holding.getStockCode(), price);
                }
            }

            for (Account account : accounts) {
                assetService.updateHoldingFinalPrices(account.getAccountId(), priceCache);
                assetService.endAccount(account.getAccountId());
            }
            //redis 캐시 삭제
            priceCache.keySet().forEach(stockCode ->
                    redisTemplate.delete(cachePrefix + stockCode)
            );
            //kafka 이벤트 발행
            List<ParticipantTotalAssetResult> totalAssets =
                    assetService.calculateTotalAssets(payload.competitionId());

            totalAssetEventProducer.sendTotalAssetCalculated(payload.competitionId(), totalAssets);

        } finally {
            String luaScript =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            "return redis.call('del', KEYS[1]) " +
                            "else return 0 end";

            stringRedisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, Long.class),
                    List.of(lockKey),
                    lockToken
            );
        }
    }
}