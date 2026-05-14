package io.antcamp.assetservice.domain.model;

import io.antcamp.assetservice.domain.exception.InvalidAmountException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Holding {

    private UUID holdingId;
    private UUID accountId;
    private String stockCode;
    private Integer stockAmount;
    private Long buyPrice;
    private Long finalPrice;

    public Holding(UUID holdingId, UUID accountId, String stockCode,
                   Integer stockAmount, Long buyPrice, Long finalPrice) {
        this.holdingId = holdingId;
        this.accountId = accountId;
        this.stockCode = stockCode;
        this.stockAmount = stockAmount;
        this.buyPrice = buyPrice;
        this.finalPrice = finalPrice;
    }

    public static Holding create(UUID accountId, String stockCode, Integer stockAmount, Long buyPrice) {
        validateAccountId(accountId);
        validateStockCode(stockCode);
        validateStockAmount(stockAmount);
        validatePrice(buyPrice);

        return new Holding(
                UUID.randomUUID(),
                accountId,
                stockCode,
                stockAmount,
                buyPrice,
                0L
        );
    }

    private static void validateAccountId(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId는 필수입니다.");
        }
    }

    private static void validateStockCode(String stockCode) {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("stockCode는 필수입니다.");
        }
    }

    public void buy(Integer quantity, Long price) {
        validateStockAmount(quantity);
        validatePrice(price);

        long totalBuyPrice = (this.buyPrice * this.stockAmount) + (price * quantity);
        int totalAmount = this.stockAmount + quantity;

        this.buyPrice = totalBuyPrice / totalAmount;
        this.stockAmount = totalAmount;
    }

    public void sell(Integer quantity) {
        validateStockAmount(quantity);

        if (this.stockAmount < quantity) {
            throw new InvalidAmountException("보유 수량보다 많이 매도할 수 없습니다.");
        }

        this.stockAmount -= quantity;
    }

    public boolean isEmpty() {
        return this.stockAmount == 0;
    }

    private static void validateStockAmount(Integer stockAmount) {
        if (stockAmount == null || stockAmount <= 0) {
            throw new InvalidAmountException("주식 수량은 0보다 커야 합니다.");
        }
    }

    private static void validatePrice(Long price) {
        if (price == null || price <= 0) {
            throw new InvalidAmountException("주식 가격은 0보다 커야 합니다.");
        }
    }

    public void updateFinalPrice(Long price) {
        validatePrice(price);
        this.finalPrice = price;
    }
}