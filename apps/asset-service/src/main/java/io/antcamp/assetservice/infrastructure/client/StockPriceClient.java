package io.antcamp.assetservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "stock-service")
public interface StockPriceClient {

    @GetMapping("/api/stocks/{stockCode}/price")
    Long getPriceAt(@PathVariable String stockCode, @RequestParam LocalDateTime at);

    @GetMapping("/api/stocks/{stockCode}/price/current")
    Long getCurrentPrice(@PathVariable String stockCode);
}