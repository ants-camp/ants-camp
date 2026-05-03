package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record StockList(
        @JsonProperty("stock-list")
        List<String > stockList
) {
}


