package io.antcamp.tradeservice.domain.model;

import common.entity.BaseEntity;
import common.exception.BusinessException;
import common.exception.ErrorCode;

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
        double totalPrice,
        int retryCount
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
                totalPrice,
                0
        );
    }

    public static Trade updateSuccess(Trade trade){
        return new Trade(
                trade.tradeId,
                trade.accountId,
                trade.tradeType,
                trade.tradeAt,
                trade.stockCode,
                trade.stockAmount,
                TradeStatus.SUCCESS,
                trade.totalPrice,
                trade.retryCount+1
        );
    }

    public static Trade updateFail(Trade trade){
        return new Trade(
                trade.tradeId,
                trade.accountId,
                trade.tradeType,
                trade.tradeAt,
                trade.stockCode,
                trade.stockAmount,
                TradeStatus.FAIL,
                trade.totalPrice,
                trade.retryCount+1
        );
    }
}
