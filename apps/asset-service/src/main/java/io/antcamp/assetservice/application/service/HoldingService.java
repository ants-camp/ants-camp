package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.application.dto.command.BuyHoldingCommand;
import io.antcamp.assetservice.application.dto.command.SellHoldingCommand;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.application.dto.query.HoldingResult;
import io.antcamp.assetservice.application.dto.query.TradeResult;
import io.antcamp.assetservice.domain.exception.HoldingNotFoundException;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountService accountService;

    @Transactional
    public TradeResult buy(BuyHoldingCommand command) {
        accountService.withdraw(command.getAccountId(), command.getBuyPrice() * command.getStockAmount());

        Holding holding = holdingRepository
                .findByAccountIdAndStockCodeWithLock(
                        command.getAccountId(),
                        command.getStockCode()
                )
                .map(existingHolding -> {
                    existingHolding.buy(
                            command.getStockAmount(),
                            command.getBuyPrice()
                    );
                    return existingHolding;
                })
                .orElseGet(() -> Holding.create(
                        command.getAccountId(),
                        command.getStockCode(),
                        command.getStockAmount(),
                        command.getBuyPrice()
                ));

        holdingRepository.save(holding);

        Account account = accountService.getAccountDomain(command.getAccountId());

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
    public TradeResult sell(SellHoldingCommand command) {
        Holding holding = holdingRepository
                .findByAccountIdAndStockCodeWithLock(
                        command.getAccountId(),
                        command.getStockCode()
                )
                .orElseThrow(() -> new HoldingNotFoundException("보유 주식을 찾을 수 없습니다."));

        holding.sell(command.getStockAmount());

        accountService.deposit(command.getAccountId(), command.getPrice() * command.getStockAmount());

        Account account = accountService.getAccountDomain(command.getAccountId());

        if (holding.isEmpty()) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        return new TradeResult(
                account.getUserId(),
                "SELL",
                LocalDateTime.now(),
                command.getStockCode(),
                command.getStockAmount(),
                command.getPrice()
        );
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