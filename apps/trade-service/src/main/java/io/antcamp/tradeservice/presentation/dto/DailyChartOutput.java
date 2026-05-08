package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;

/**
 * KIS FHKST03010100 output2 — 국내주식 기간별(일/주/월/년) 시세 한 봉
 *
 * KIS는 날짜를 "20260507" (YYYYMMDD) 형태로 전달.
 * 프론트로 내보낼 때는 ISO 8601 ("2026-05-07") 형태로 직렬화해야
 * 차트 라이브러리가 Unix 타임스탬프로 오해하지 않음.
 */
public record DailyChartOutput(
        @JsonProperty("stck_bsop_date")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
        LocalDate date,        // KIS: "20260507" → 응답: "2026-05-07"

        @JsonProperty("stck_oprc")  String open,        // 시가
        @JsonProperty("stck_hgpr")  String high,        // 고가
        @JsonProperty("stck_lwpr")  String low,         // 저가
        @JsonProperty("stck_clpr")  String close,       // 종가
        @JsonProperty("acml_vol")   String volume,      // 누적 거래량
        @JsonProperty("acml_tr_pbmn") String tradeAmount // 누적 거래대금
) {}
