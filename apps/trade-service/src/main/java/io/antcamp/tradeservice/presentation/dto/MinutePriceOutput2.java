package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MinutePriceOutput2(
        @JsonProperty("stck_bsop_date")
        String date,
        @JsonProperty("stck_cntg_hour")
        String time,
        @JsonProperty("stck_prpr")
        String priceNow,
        @JsonProperty("stck_oprc")
        String priceStart,
        @JsonProperty("stck_hgpr")
        String priceHigh,
        @JsonProperty("stck_lwpr")
        String priceLow,
        @JsonProperty("cntg_vol")
        String dealVolumeCount,
        @JsonProperty("acml_tr_pbmn")
        String acmlTrPbmn
) {
}
