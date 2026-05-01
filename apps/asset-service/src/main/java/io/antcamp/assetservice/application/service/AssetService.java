package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.application.dto.query.AssetResult;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.application.dto.query.ParticipantTotalAssetResult;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import io.antcamp.assetservice.domain.exception.AccountNotFoundException;
import io.antcamp.assetservice.domain.exception.UnauthorizedAccountAccessException;
import io.antcamp.assetservice.infrastructure.client.StockPriceClient;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.TotalAssetCalculatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceClient stockPriceClient;

    @Transactional(readOnly = true)
    public AssetResult getAsset(UUID accountId, UUID userId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));

        if (!account.getUserId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("해당 계좌에 접근할 권한이 없습니다.");
        }

        List<Holding> holdings = holdingRepository.findAllByAccountId(accountId);

        long holdingEvaluationAmount = 0L;

        for (Holding holding : holdings) {
            Long price;

            if (account.isEnded()) {
                price = holding.getFinalPrice();
            } else {
                price = stockPriceClient.getCurrentPrice(holding.getStockCode());
            }

            holdingEvaluationAmount += price * holding.getStockAmount();
        }

        Long totalAssetAmount = account.getAccountAmount() + holdingEvaluationAmount;

        return new AssetResult(
                account.getAccountId(),
                account.getAccountAmount(),
                holdingEvaluationAmount,
                totalAssetAmount
        );
    }

    public List<ParticipantTotalAssetResult> calculateTotalAssets(UUID competitionId) {
        List<Account> accounts = accountRepository.findAllByCompetitionId(competitionId);

        return accounts.stream()
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
    }
}