package io.antcamp.tradeservice.application.service;

import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.dto.*;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TradeService {
    AccessTokenResponse requestAccessToken();   // REST API 토큰 (/oauth2/tokenP)
    String requestApprovalKey();           // WebSocket 접속키 (/oauth2/Approval)
    void clearAll();
    double getMinutePrice(String stockCode, LocalDateTime dateTime);
    double getNowPrice(String stockCode, LocalDateTime dateTime);
    BuyStockResponse buyStock(LocalDateTime time, String stockCode, int stockAmount, UUID accountId);

    StockPriceList stockPriceList(StockList stockList, LocalDateTime dateTime);

    SellStockResponse sellStock(LocalDateTime now, String stockCode, int stockAmount, UUID accountId);
}
