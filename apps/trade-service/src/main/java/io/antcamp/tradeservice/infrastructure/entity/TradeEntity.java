package io.antcamp.tradeservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.tradeservice.domain.model.OrderType;
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
@Table(name = "p_trade", indexes = {
        @Index(name = "idx_trade_status_order_type", columnList = "tradeStatus, orderType")
})
public class TradeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tradeId;

    @Column(nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType tradeType;

    private LocalDateTime tradeAt;

    @Column(nullable = false)
    private String stockCode;

    @Column(nullable = false)
    private int stockAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus tradeStatus;

    @Column(nullable = false)
    private double totalPrice;

    /** 시장가(MARKET) / 지정가(LIMIT) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    /** 지정가 주문일 때만 값 있음, 시장가는 null */
    private Double limitPrice;

    // ── 변환 ──────────────────────────────────────────────────────────────

    public Trade toDomain() {
        // Trade.create()는 항상 PENDING을 세팅하므로 fromPersistence 사용
        return Trade.fromPersistence(
                this.tradeId,
                this.accountId,
                this.tradeType,
                this.tradeAt,
                this.stockCode,
                this.stockAmount,
                this.tradeStatus,
                this.totalPrice,
                0,              // retryCount 는 엔티티에 미저장 — 메모리에서만 관리
                this.orderType,
                this.limitPrice
        );
    }

    public void updateStatus(Trade trade) {
        this.tradeStatus = trade.tradeStatus();
    }

    public static TradeEntity fromDomain(Trade trade) {
        return new TradeEntity(trade);
    }

    private TradeEntity(Trade trade) {
        this.tradeId    = trade.tradeId();
        this.accountId  = trade.accountId();
        this.tradeType  = trade.tradeType();
        this.tradeAt    = trade.tradeAt();
        this.stockCode  = trade.stockCode();
        this.stockAmount = trade.stockAmount();
        this.tradeStatus = trade.tradeStatus();
        this.totalPrice  = trade.totalPrice();
        this.orderType   = trade.orderType();
        this.limitPrice  = trade.limitPrice();
    }
}
