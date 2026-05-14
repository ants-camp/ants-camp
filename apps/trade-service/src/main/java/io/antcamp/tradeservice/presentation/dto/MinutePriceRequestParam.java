package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public record MinutePriceRequestParam(
        @JsonProperty("FID_COND_MRKT_DIV_CODE")
        String marketDivCode,
        @JsonProperty("FID_INPUT_ISCD")
        String stockCode,
        @JsonProperty("FID_INPUT_HOUR_1")
        String time,
        @JsonProperty("FID_INPUT_DATE_1")
        String date,
        @JsonProperty("FID_PW_DATA_INCU_YN")
        String includePastData,
        @JsonProperty("FID_FAKE_TICK_INCU_YN")
        String includeFakeData
) {
    public static Map<String , Object> create(String stockCode, String time, String date) {
        return Map.of(
                "FID_COND_MRKT_DIV_CODE", "J",
                "FID_INPUT_ISCD",          stockCode,
                "FID_INPUT_HOUR_1",        time,
                "FID_INPUT_DATE_1",        date,
                "FID_PW_DATA_INCU_YN",     "Y",   // Y=기준시각 이전 캔들 포함(차트용), N=해당 시각만 → 빈 배열
                "FID_FAKE_TICK_INCU_YN",   "N"    // 공백(" ") → "N" (KIS 필수 Y/N)
        );
    }
}
