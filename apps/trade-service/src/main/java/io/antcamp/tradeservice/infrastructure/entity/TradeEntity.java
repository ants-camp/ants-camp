package io.antcamp.tradeservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.tradeservice.domain.model.Trade;
import io.antcamp.tradeservice.domain.model.TradeStatus;
import io.antcamp.tradeservice.domain.model.TradeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_trade")
public class TradeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tradeId;
    @Column(nullable = false)
    private UUID accountId;
    @Enumerated(value = EnumType.STRING)
    private TradeType tradeType;
    private LocalDateTime tradeAt;
    @Column(nullable = false)
    private String stockCode;
    @Column(nullable = false)
    private int stockAmount;
    @Enumerated(value = EnumType.STRING)
    private TradeStatus tradeStatus;
    @Column(nullable = false)
    private double totalPrice;

    public Trade toDomain(){
        return Trade.create(
                this.tradeId,
                this.accountId,
                this.tradeType,
                this.tradeAt,
                this.stockCode,
                this.stockAmount,
                this.totalPrice
        );
    }

    public void updateStatus(Trade trade){
        this.tradeStatus = trade.tradeStatus();
    }

    public static TradeEntity fromDomain(Trade trade){
        return new TradeEntity(trade);
    }

    private TradeEntity(Trade trade){
        this.tradeId = trade.tradeId();
        this.accountId = trade.accountId();
        this.tradeType = trade.tradeType();
        this.tradeAt = trade.tradeAt();
        this.stockCode = trade.stockCode();
        this.stockAmount = trade.stockAmount();
        this.tradeStatus = trade.tradeStatus();
        this.totalPrice = trade.totalPrice();
    }
}
