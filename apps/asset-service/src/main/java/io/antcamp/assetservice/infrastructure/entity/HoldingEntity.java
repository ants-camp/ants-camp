package io.antcamp.assetservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.assetservice.domain.model.Holding;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.SQLRestriction;
import java.util.UUID;

@Entity
@Table(
        name = "p_holdings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_holdings_account_stock",
                        columnNames = {"account_id", "stock_code"}
                )
        }
)
@SQLRestriction("deleted_at is NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class HoldingEntity extends BaseEntity {

    @Id
    private UUID holdingId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(nullable = false)
    private Integer stockAmount;

    @Column(nullable = false)
    private Long buyPrice;

    @Column(nullable = false)
    private Long finalPrice;

    private HoldingEntity(UUID holdingId, UUID accountId, String stockCode,
                          Integer stockAmount, Long buyPrice, Long finalPrice) {
        this.holdingId = holdingId;
        this.accountId = accountId;
        this.stockCode = stockCode;
        this.stockAmount = stockAmount;
        this.buyPrice = buyPrice;
        this.finalPrice = finalPrice;
    }

    public static HoldingEntity from(Holding holding) {
        return new HoldingEntity(
                holding.getHoldingId(),
                holding.getAccountId(),
                holding.getStockCode(),
                holding.getStockAmount(),
                holding.getBuyPrice(),
                holding.getFinalPrice()
        );
    }

    public Holding toDomain() {
        return new Holding(
                holdingId,
                accountId,
                stockCode,
                stockAmount,
                buyPrice,
                finalPrice
        );
    }
}