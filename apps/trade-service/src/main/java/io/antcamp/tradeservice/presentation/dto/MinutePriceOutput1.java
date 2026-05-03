package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MinutePriceOutput1(
        @JsonProperty("prdy_vrss")
        String prdyVrss,
        @JsonProperty("prdy_vrss_sign")
        String prdyVrssSign,
        @JsonProperty("prdy_ctrt")
        String prdyCtrt,
        @JsonProperty("acml_vol")
        String acmlVol,
        @JsonProperty("acml_tr_pbmn")
        String acmlTrPbmn,
        @JsonProperty("hts_kor_isnm")
        String stockName,
        @JsonProperty("stck_prpr")
        String priceNow
) {
}
