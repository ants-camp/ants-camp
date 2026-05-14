package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import io.antcamp.assetservice.infrastructure.client.StockPriceClient;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceClient stockPriceClient;
    private final RedisTemplate<String, String> stringRedisTemplate;

    public void updateRanking(UUID competitionId) {
        log.info("[Ranking] 랭킹 업데이트 시작. competitionId={}", competitionId);
        List<Account> accounts = accountRepository.findAllByCompetitionId(competitionId);

        Map<String, Long> priceCache = new HashMap<>();

        for (Account account : accounts) {
            try {
                List<Holding> holdings = holdingRepository.findAllByAccountId(account.getAccountId());
                log.info("[Ranking] 계좌 처리. accountId={}, 현금잔액={}, 보유종목수={}",
                        account.getAccountId(), account.getAccountAmount(), holdings.size());

                long holdingEvaluationAmount = 0L;
                for (Holding holding : holdings) {
                    Long price = priceCache.computeIfAbsent(
                            holding.getStockCode(),
                            stockCode -> {
                                Double p = stockPriceClient.getCurrentPrice(stockCode, LocalDateTime.now()).getData();
                                log.info("[Ranking] 현재가 조회. stockCode={}, price={}", stockCode, p);
                                return p == null ? 0L : p.longValue();
                            }
                    );
                    holdingEvaluationAmount += price * holding.getStockAmount();
                    log.info("[Ranking] 보유종목 평가. stockCode={}, amount={}, price={}, 평가액={}",
                            holding.getStockCode(), holding.getStockAmount(), price, price * holding.getStockAmount());
                }

                long totalAsset = account.getAccountAmount() + holdingEvaluationAmount;
                log.info("[Ranking] 총자산 계산. accountId={}, userId={}, 현금={}, 보유평가={}, 총자산={}",
                        account.getAccountId(), account.getUserId(),
                        account.getAccountAmount(), holdingEvaluationAmount, totalAsset);

                String rankingKey = "ranking:competition:" + competitionId;
                Boolean result = stringRedisTemplate.opsForZSet().add(rankingKey, account.getUserId().toString(), totalAsset);
                log.info("[Ranking] Redis ZSet 기록. key={}, userId={}, totalAsset={}, result={}",
                        rankingKey, account.getUserId(), totalAsset, result);

            } catch (Exception e) {
                log.error("[Ranking] 계좌 처리 실패. accountId={}, error={}", account.getAccountId(), e.getMessage(), e);
            }
        }

        log.info("[Ranking] 랭킹 업데이트 완료. competitionId={}, 참가자수={}", competitionId, accounts.size());
    }
}
