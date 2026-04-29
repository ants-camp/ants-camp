package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.application.dto.query.AssetResult;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AccountService accountService;
    private final HoldingRepository holdingRepository;

    @Transactional(readOnly = true)
    public AssetResult getAsset(UUID accountId, UUID userId) {

        // 계좌 조회 (권한 체크 포함)
        AccountResult account = accountService.getAccount(accountId, userId);

        // 보유 주식 조회
        List<Holding> holdings = holdingRepository.findAllByAccountId(accountId);

        long holdingEvaluationAmount = 0L;

        // 대회 상태 확인 (CompetitionService 등에서 가져와야 함)
        boolean isCompetitionEnded = false;

        for (Holding holding : holdings) {

            Long price;

            if (isCompetitionEnded) {
                // 대회 종료 → finalPrice 사용
                price = holding.getFinalPrice();
            } else {
                // 현재가 조회
                // price = stockPriceService.getCurrentPrice(holding.getStockCode());

                // 임시 처리
                price = holding.getFinalPrice();
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
}