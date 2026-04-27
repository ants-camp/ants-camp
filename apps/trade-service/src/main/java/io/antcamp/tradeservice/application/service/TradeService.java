package io.antcamp.tradeservice.application.service;

import io.antcamp.tradeservice.presentation.dto.KisAccessToken;

public interface TradeService {
    KisAccessToken requestAccessToken();
    void clearAll();
}
