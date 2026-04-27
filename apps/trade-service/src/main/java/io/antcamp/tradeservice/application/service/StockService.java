package io.antcamp.tradeservice.application.service;

import io.antcamp.tradeservice.infrastructure.dto.StockSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 종목 검색 서비스 (국내주식 전용)
 *
 * 현재 구현: 인메모리 종목 마스터 (주요 KOSPI/KOSDAQ 50종목)
 * 확장 포인트: KIS REST API 전종목 마스터 파일을 주기적으로 동기화 후 DB 조회로 전환
 */
@Service
public class StockService {

    private static final List<StockSearchResponse> STOCK_MASTER = List.of(
            // ── KOSPI 대형주 ──────────────────────────────────────────
            new StockSearchResponse("005930", "삼성전자",        "KOSPI"),
            new StockSearchResponse("000660", "SK하이닉스",      "KOSPI"),
            new StockSearchResponse("035420", "NAVER",           "KOSPI"),
            new StockSearchResponse("035720", "카카오",          "KOSPI"),
            new StockSearchResponse("005380", "현대차",          "KOSPI"),
            new StockSearchResponse("051910", "LG화학",          "KOSPI"),
            new StockSearchResponse("006400", "삼성SDI",         "KOSPI"),
            new StockSearchResponse("207940", "삼성바이오로직스", "KOSPI"),
            new StockSearchResponse("068270", "셀트리온",        "KOSPI"),
            new StockSearchResponse("373220", "LG에너지솔루션",  "KOSPI"),
            new StockSearchResponse("000270", "기아",            "KOSPI"),
            new StockSearchResponse("105560", "KB금융",          "KOSPI"),
            new StockSearchResponse("055550", "신한지주",        "KOSPI"),
            new StockSearchResponse("012330", "현대모비스",      "KOSPI"),
            new StockSearchResponse("028260", "삼성물산",        "KOSPI"),
            new StockSearchResponse("018260", "삼성에스디에스",  "KOSPI"),
            new StockSearchResponse("003670", "포스코홀딩스",    "KOSPI"),
            new StockSearchResponse("034730", "SK",              "KOSPI"),
            new StockSearchResponse("030200", "KT",              "KOSPI"),
            new StockSearchResponse("017670", "SK텔레콤",        "KOSPI"),
            new StockSearchResponse("011200", "HMM",             "KOSPI"),
            new StockSearchResponse("032830", "삼성생명",        "KOSPI"),
            new StockSearchResponse("009150", "삼성전기",        "KOSPI"),
            new StockSearchResponse("066570", "LG전자",          "KOSPI"),
            new StockSearchResponse("024110", "기업은행",        "KOSPI"),
            new StockSearchResponse("316140", "우리금융지주",    "KOSPI"),
            new StockSearchResponse("352820", "하이브",          "KOSPI"),
            new StockSearchResponse("003550", "LG",              "KOSPI"),
            new StockSearchResponse("086790", "하나금융지주",    "KOSPI"),
            new StockSearchResponse("011070", "LG이노텍",        "KOSPI"),
            new StockSearchResponse("096770", "SK이노베이션",    "KOSPI"),
            new StockSearchResponse("000810", "삼성화재",        "KOSPI"),
            new StockSearchResponse("329180", "현대중공업",      "KOSPI"),
            new StockSearchResponse("010950", "S-Oil",           "KOSPI"),
            new StockSearchResponse("251270", "넷마블",          "KOSPI"),
            new StockSearchResponse("036570", "엔씨소프트",      "KOSPI"),
            new StockSearchResponse("042700", "한미반도체",      "KOSPI"),
            new StockSearchResponse("241560", "두산밥캣",        "KOSPI"),
            new StockSearchResponse("272210", "한화시스템",      "KOSPI"),
            // ── KOSDAQ ───────────────────────────────────────────────
            new StockSearchResponse("196170", "알테오젠",        "KOSDAQ"),
            new StockSearchResponse("086520", "에코프로",        "KOSDAQ"),
            new StockSearchResponse("041510", "에스엠",          "KOSDAQ"),
            new StockSearchResponse("263750", "펄어비스",        "KOSDAQ"),
            new StockSearchResponse("293490", "카카오게임즈",    "KOSDAQ"),
            new StockSearchResponse("035900", "JYP Ent.",        "KOSDAQ"),
            new StockSearchResponse("091990", "셀트리온헬스케어","KOSDAQ"),
            new StockSearchResponse("326030", "SK바이오팜",      "KOSDAQ"),
            new StockSearchResponse("122630", "KODEX 레버리지",  "KOSPI"),
            new StockSearchResponse("069500", "KODEX 200",       "KOSPI"),
            new StockSearchResponse("114800", "KODEX 인버스",    "KOSPI")
    );

    /**
     * 종목명 또는 종목코드로 검색한다.
     *
     * @param keyword 검색어 (공백 시 전체 반환)
     * @param limit   최대 반환 건수 (기본 20)
     */
    public List<StockSearchResponse> search(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return STOCK_MASTER.stream().limit(limit).collect(Collectors.toList());
        }
        String q = keyword.trim().toLowerCase(Locale.ROOT);
        return STOCK_MASTER.stream()
                .filter(s -> s.name().toLowerCase(Locale.ROOT).contains(q)
                          || s.code().contains(q))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
