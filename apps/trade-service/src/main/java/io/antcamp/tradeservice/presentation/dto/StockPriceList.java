package io.antcamp.tradeservice.presentation.dto;

import java.util.Map;

public record StockPriceList(
        Map<String ,String > stockPrice
) {

}
