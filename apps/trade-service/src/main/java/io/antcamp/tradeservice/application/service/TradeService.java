package io.antcamp.tradeservice.application.service;

import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.dto.BuyStockRequest;
import io.antcamp.tradeservice.presentation.dto.BuyStockResponse;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import io.antcamp.tradeservice.presentation.dto.MinutePriceResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TradeService {
    AccessTokenResponse requestAccessToken();   // REST API 토큰 (/oauth2/tokenP)
    String requestApprovalKey();           // WebSocket 접속키 (/oauth2/Approval)
    void clearAll();
    double getMinutePrice(String stockCode, LocalDateTime dateTime);
    double getNowPrice(String stockCode, LocalDateTime dateTime);
    BuyStockResponse buyStock(LocalDateTime time, String stockCode, int stockAmount, UUID accountId);
}
