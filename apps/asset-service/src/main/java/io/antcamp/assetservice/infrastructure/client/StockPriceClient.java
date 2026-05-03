package io.antcamp.assetservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "stock-service")
public interface StockPriceClient {

    @GetMapping("/api/trades/minute-price")
    Long getPriceAt(
            @PathVariable("stockCode") String stockCode,
            @RequestParam("at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at
    );

    @GetMapping("/api/trades/now-price")
    Long getCurrentPrice(@PathVariable("stockCode") String stockCode);
}