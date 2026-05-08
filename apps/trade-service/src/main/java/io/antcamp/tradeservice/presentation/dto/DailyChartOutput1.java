package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KIS FHKST03010100 output1 — 종목 기본 정보
 */
public record DailyChartOutput1(
        @JsonProperty("hts_kor_isnm") String stockName,   // 종목명
        @JsonProperty("stck_prpr")    String currentPrice, // 현재가
        @JsonProperty("prdy_vrss")    String priceChange,  // 전일대비
        @JsonProperty("prdy_ctrt")    String changeRate,   // 전일대비율
        @JsonProperty("acml_vol")     String volume        // 누적거래량
) {}
