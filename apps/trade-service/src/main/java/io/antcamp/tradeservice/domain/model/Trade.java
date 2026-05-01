package io.antcamp.tradeservice.domain.model;

import common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record Trade(
        UUID tradeId,
        UUID accountId,
        TradeType tradeType,
        LocalDateTime tradeAt,
        String stockCode,
        int stockAmount,
        TradeStatus tradeStatus,
        double totalPrice
) {

    public static Trade create(UUID tradeId, UUID accountId, TradeType tradeType,LocalDateTime tradeAt, String stockCode, int stockAmount, double totalPrice){
        return new Trade(
                tradeId,
                accountId,
                tradeType,
                tradeAt,
                stockCode,
                stockAmount,
                TradeStatus.PENDING,
                totalPrice
        );
    }
}
