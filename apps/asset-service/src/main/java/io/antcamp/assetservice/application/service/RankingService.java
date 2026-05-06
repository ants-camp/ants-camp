package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import io.antcamp.assetservice.infrastructure.client.StockPriceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceClient stockPriceClient;
    private final RedisTemplate<String, String> stringRedisTemplate;

    public void updateRanking(UUID competitionId) {
        List<Account> accounts = accountRepository.findAllByCompetitionId(competitionId);

        Map<String, Long> priceCache = new HashMap<>();

        for (Account account : accounts) {
            List<Holding> holdings = holdingRepository.findAllByAccountId(account.getAccountId());

            long holdingEvaluationAmount = 0L;
            for (Holding holding : holdings) {
                Long price = priceCache.computeIfAbsent(
                        holding.getStockCode(),
                        stockCode -> stockPriceClient.getCurrentPrice(stockCode)
                );
                holdingEvaluationAmount += price * holding.getStockAmount();
            }

            long totalAsset = account.getAccountAmount() + holdingEvaluationAmount;

            String rankingKey = "ranking:competition:" + competitionId;
            stringRedisTemplate.opsForZSet().add(rankingKey, account.getUserId().toString(), totalAsset);
        }

        log.info("랭킹 업데이트 완료: competitionId={}", competitionId);
    }
}