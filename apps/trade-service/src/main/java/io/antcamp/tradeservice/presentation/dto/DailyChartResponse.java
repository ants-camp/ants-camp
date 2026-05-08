package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * KIS FHKST03010100 응답 — 국내주식 기간별 시세 (일/주/월/년봉)
 */
public record DailyChartResponse(
        @JsonProperty("output1") DailyChartOutput1  output1,
        @JsonProperty("output2") List<DailyChartOutput> output2,
        @JsonProperty("rt_cd")   String rtCd,
        @JsonProperty("msg_cd")  String msgCd,
        @JsonProperty("msg1")    String msg1
) {
    public static DailyChartResponse empty() {
        return new DailyChartResponse(null, Collections.emptyList(), "1", "FALLBACK", "서비스 일시 불가");
    }
}
