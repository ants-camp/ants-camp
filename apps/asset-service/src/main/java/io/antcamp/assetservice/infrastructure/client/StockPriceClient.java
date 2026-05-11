package io.antcamp.assetservice.infrastructure.client;

import common.dto.CommonResponse;
import java.time.LocalDateTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "trade-service")
public interface StockPriceClient {

    @GetMapping("/api/trades/minute-price")
    CommonResponse<Double> getPriceAt(
            @RequestParam("stock_code") String stockCode,
            @RequestParam("date_time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at
    );

    @GetMapping("/api/trades/now-price")
    CommonResponse<Double> getCurrentPrice(
            @RequestParam("stock_code") String stockCode,
            @RequestParam("date_time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    );
}
