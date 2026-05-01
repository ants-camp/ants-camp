package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.service.AssetService;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import io.antcamp.assetservice.domain.repository.TotalAssetEventProducer;
import io.antcamp.assetservice.infrastructure.client.StockPriceClient;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.CompetitionEndedEvent;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.TotalAssetCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionEndedEventConsumer {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceClient stockPriceClient;
    private final RedisTemplate<String, Long> redisTemplate;
    private final AssetService assetService;
    private final TotalAssetEventProducer totalAssetEventProducer;

    @Transactional
    @KafkaListener(
            topics = "${topics.competition.ended}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleCompetitionEnded(CompetitionEndedEvent payload) {

        String cachePrefix = "stock:price:" + payload.competitionId() + ":";

        List<Account> accounts = accountRepository.findAllByCompetitionId(payload.competitionId());

        for (Account account : accounts) {
            List<Holding> holdings = holdingRepository.findAllByAccountId(account.getAccountId());

            for (Holding holding : holdings) {
                String cacheKey = cachePrefix + holding.getStockCode();

                Long price = redisTemplate.opsForValue().get(cacheKey);
                if (price == null) {
                    price = stockPriceClient.getPriceAt(holding.getStockCode(), payload.endedAt());
                    redisTemplate.opsForValue().set(cacheKey, price, 1, TimeUnit.HOURS);
                }

                holding.updateFinalPrice(price);
                holdingRepository.save(holding);
            }

            account.end();
            accountRepository.save(account);
        }

        accounts.forEach(account ->
                holdingRepository.findAllByAccountId(account.getAccountId())
                        .forEach(holding -> redisTemplate.delete(cachePrefix + holding.getStockCode()))
        );

        List<TotalAssetCalculatedEvent.ParticipantTotalAsset> totalAssets =
                assetService.calculateTotalAssets(payload.competitionId());

        totalAssetEventProducer.sendTotalAssetCalculated(
                new TotalAssetCalculatedEvent(payload.competitionId(), totalAssets)
        );
    }
}