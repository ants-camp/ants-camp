package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.application.dto.query.AssetResult;
import io.antcamp.assetservice.application.dto.query.ParticipantTotalAssetResult;
import io.antcamp.assetservice.domain.exception.AccountNotFoundException;
import io.antcamp.assetservice.domain.exception.UnauthorizedAccountAccessException;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import io.antcamp.assetservice.infrastructure.client.StockPriceClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceClient stockPriceClient;

    public AssetResult getAsset(UUID accountId, UUID userId) {
        log.info("[Asset] 자산 조회 요청. accountId={}, userId={}", accountId, userId);
        Account account = getAccountWithValidation(accountId, userId);
        List<Holding> holdings = holdingRepository.findAllByAccountId(accountId);

        long holdingEvaluationAmount = 0L;

        for (Holding holding : holdings) {
            double price;

            if (account.isEnded()) {
                price = Double.parseDouble(String.valueOf(holding.getFinalPrice()));
            } else {
                price = stockPriceClient.getCurrentPrice(holding.getStockCode(), LocalDateTime.now()).getData();
            }

            holdingEvaluationAmount += (long) (price * holding.getStockAmount());
        }

        Long totalAssetAmount = account.getAccountAmount() + holdingEvaluationAmount;

        log.info("[Asset] 자산 조회 완료. accountId={}, 현금={}, 평가금액={}, 총자산={}",
                accountId, account.getAccountAmount(), holdingEvaluationAmount, totalAssetAmount);
        return new AssetResult(
                account.getAccountId(),
                account.getAccountAmount(),
                holdingEvaluationAmount,
                totalAssetAmount
        );
    }

    @Transactional(readOnly = true)
    public Account getAccountWithValidation(UUID accountId, UUID userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));

        if (!account.getUserId().equals(userId)) {
            log.warn("[Asset] 권한 없는 계좌 접근 시도. accountId={}, 요청userId={}, 계좌ownerId={}",
                    accountId, userId, account.getUserId());
            throw new UnauthorizedAccountAccessException("해당 계좌에 접근할 권한이 없습니다.");
        }
        return account;
    }

    public List<ParticipantTotalAssetResult> calculateTotalAssets(UUID competitionId) {
        log.info("[Asset] 대회 전체 참가자 총자산 계산 시작. competitionId={}", competitionId);
        List<Account> accounts = accountRepository.findAllByCompetitionId(competitionId);

        List<ParticipantTotalAssetResult> results = accounts.stream()
                .map(account -> {
                    List<Holding> holdings = holdingRepository.findAllByAccountId(account.getAccountId());

                    long holdingEvaluationAmount = holdings.stream()
                            .mapToLong(h -> h.getFinalPrice() * h.getStockAmount())
                            .sum();

                    long totalAsset = account.getAccountAmount() + holdingEvaluationAmount;

                    return new ParticipantTotalAssetResult(
                            account.getUserId(),
                            totalAsset
                    );
                })
                .toList();

        log.info("[Asset] 대회 전체 참가자 총자산 계산 완료. competitionId={}, 참가자수={}",
                competitionId, results.size());
        return results;
    }

    @Transactional
    public void updateHoldingFinalPrices(UUID accountId, Map<String, Long> priceCache) {
        log.info("[Asset] 보유주식 최종가격 업데이트 시작. accountId={}", accountId);
        List<Holding> holdings = holdingRepository.findAllByAccountId(accountId);

        for (Holding holding : holdings) {
            Long price = priceCache.get(holding.getStockCode());
            if (price == null) {
                log.warn("[Asset] 종목 가격 정보 없음. accountId={}, stockCode={}",
                        accountId, holding.getStockCode());
                throw new IllegalStateException("종목 가격 정보가 없습니다. stockCode=" + holding.getStockCode());
            }
            holding.updateFinalPrice(price);
            holdingRepository.save(holding);
        }
        log.info("[Asset] 보유주식 최종가격 업데이트 완료. accountId={}, 종목수={}", accountId, holdings.size());
    }

    @Transactional
    public void endAccount(UUID accountId) {
        log.info("[Asset] 계좌 종료 요청. accountId={}", accountId);
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));

        account.end();
        accountRepository.save(account);
        log.info("[Asset] 계좌 종료 완료. accountId={}", accountId);
    }
}