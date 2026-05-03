package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.application.dto.command.BuyHoldingCommand;
import io.antcamp.assetservice.application.dto.command.SellHoldingCommand;
import io.antcamp.assetservice.application.dto.query.HoldingResult;
import io.antcamp.assetservice.domain.exception.HoldingNotFoundException;
import io.antcamp.assetservice.domain.model.Holding;
import io.antcamp.assetservice.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;

    @Transactional
    public void buy(BuyHoldingCommand command) {
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
    }

    @Transactional
    public void sell(SellHoldingCommand command) {
        Holding holding = holdingRepository
                .findByAccountIdAndStockCodeWithLock(
                        command.getAccountId(),
                        command.getStockCode()
                )
                .orElseThrow(() -> new HoldingNotFoundException("보유 주식을 찾을 수 없습니다."));

        holding.sell(command.getStockAmount());

        if (holding.isEmpty()) {
            holdingRepository.delete(holding);
            return;
        }

        holdingRepository.save(holding);
    }

    @Transactional(readOnly = true)
    public List<HoldingResult> getHoldings(UUID accountId) {
        return holdingRepository.findAllByAccountId(accountId)
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