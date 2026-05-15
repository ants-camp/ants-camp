package io.antcamp.assetservice.application.service;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.assetservice.application.dto.command.BuyHoldingCommand;
import io.antcamp.assetservice.application.dto.command.SellHoldingCommand;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.application.dto.query.HoldingResult;
import io.antcamp.assetservice.application.dto.query.TradeResult;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountService accountService;

    @Transactional
    public TradeResult buy(BuyHoldingCommand command, UUID userId) {
        log.info("[Holding] 매수 요청. accountId={}, stockCode={}, amount={}, price={}",
                command.getAccountId(), command.getStockCode(),
                command.getStockAmount(), command.getBuyPrice());

        Account account = accountService.getAccountDomain(command.getAccountId());

        if (!account.getUserId().equals(userId)) {
            log.warn("[Holding] 권한 없는 매수 시도. accountId={}, 요청userId={}, 계좌ownerId={}",
                    command.getAccountId(), userId, account.getUserId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS); // ✅ 변경
        }

        if (account.isEnded()) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT); // ✅ 변경
        }

        accountService.withdraw(command.getAccountId(), command.getBuyPrice() * command.getStockAmount());

        Holding holding = holdingRepository
                .findByAccountIdAndStockCodeWithLock(command.getAccountId(), command.getStockCode())
                .map(existingHolding -> {
                    existingHolding.buy(command.getStockAmount(), command.getBuyPrice());
                    return existingHolding;
                })
                .orElseGet(() -> Holding.create(
                        command.getAccountId(),
                        command.getStockCode(),
                        command.getStockAmount(),
                        command.getBuyPrice()
                ));

        holdingRepository.save(holding);

        log.info("[Holding] 매수 완료. accountId={}, stockCode={}, amount={}, price={}",
                command.getAccountId(), command.getStockCode(),
                command.getStockAmount(), command.getBuyPrice());

        return new TradeResult(
                account.getUserId(),
                "BUY",
                LocalDateTime.now(),
                command.getStockCode(),
                command.getStockAmount(),
                command.getBuyPrice()
        );
    }

    @Transactional
    public TradeResult sell(SellHoldingCommand command, UUID userId) {
        log.info("[Holding] 매도 요청. accountId={}, stockCode={}, amount={}, price={}",
                command.getAccountId(), command.getStockCode(),
                command.getStockAmount(), command.getPrice());

        Account account = accountService.getAccountDomain(command.getAccountId());

        if (!account.getUserId().equals(userId)) {
            log.warn("[Holding] 권한 없는 매도 시도. accountId={}, 요청userId={}, 계좌ownerId={}",
                    command.getAccountId(), userId, account.getUserId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS); // ✅ 변경
        }

        if (account.isEnded()) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT); // ✅ 변경
        }

        Holding holding = holdingRepository
                .findByAccountIdAndStockCodeWithLock(command.getAccountId(), command.getStockCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOLDING_NOT_FOUND)); // ✅ 변경

        holding.sell(command.getStockAmount());

        accountService.deposit(command.getAccountId(), command.getPrice() * command.getStockAmount());

        if (holding.isEmpty()) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        log.info("[Holding] 매도 완료. accountId={}, stockCode={}, amount={}, price={}",
                command.getAccountId(), command.getStockCode(),
                command.getStockAmount(), command.getPrice());

        return new TradeResult(
                account.getUserId(),
                "SELL",
                LocalDateTime.now(),
                command.getStockCode(),
                command.getStockAmount(),
                command.getPrice()
        );
    }

    /**
     * 내부 서비스 호출용 — 특정 계좌의 특정 종목 보유 수량 조회.
     * 권한 검증을 생략하므로 게이트웨이 외부 노출 금지 (`/internal/...` 경로 사용).
     * 보유가 없으면 0 반환.
     */
    @Transactional(readOnly = true)
    public int getHoldingQuantity(UUID accountId, String stockCode) {
        return holdingRepository
                .findByAccountIdAndStockCode(accountId, stockCode)
                .map(Holding::getStockAmount)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public List<HoldingResult> getHoldings(UUID accountId, UUID userId) {
        AccountResult account = accountService.getAccount(accountId, userId);

        return holdingRepository.findAllByAccountId(account.getAccountId())
                .stream()
                .map(holding -> new HoldingResult(
                        holding.getHoldingId(),
                        holding.getAccountId(),
                        holding.getStockCode(),
                        holding.getStockAmount(),
                        holding.getBuyPrice(),
                        holding.getFinalPrice()
                ))
                .toList();
    }
}