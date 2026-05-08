package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record MinutePriceOutput2(
        @JsonProperty("stck_bsop_date")
        String date,            // KIS 원본: "20260507"

        @JsonProperty("stck_cntg_hour")
        String time,            // KIS 원본: "113016"

        @JsonProperty("stck_prpr")    String priceNow,
        @JsonProperty("stck_oprc")    String priceStart,
        @JsonProperty("stck_hgpr")    String priceHigh,
        @JsonProperty("stck_lwpr")    String priceLow,
        @JsonProperty("cntg_vol")     String dealVolumeCount,
        @JsonProperty("acml_tr_pbmn") String acmlTrPbmn
) {
    private static final DateTimeFormatter KIS_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter KIS_TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    /**
     * 날짜("20260507") + 시간("113016") → ISO 8601 LocalDateTime ("2026-05-07T11:30:16")
     * 프론트 차트 라이브러리가 타임스탬프를 올바르게 파싱할 수 있도록 제공.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime datetime() {
        try {
            LocalDate d = LocalDate.parse(date, KIS_DATE_FMT);
            int h = Integer.parseInt(time.substring(0, 2));
            int m = Integer.parseInt(time.substring(2, 4));
            int s = Integer.parseInt(time.substring(4, 6));
            return d.atTime(h, m, s);
        } catch (Exception e) {
            return null;
        }
    }
}
