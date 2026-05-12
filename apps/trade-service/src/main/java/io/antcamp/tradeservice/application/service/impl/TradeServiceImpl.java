package io.antcamp.tradeservice.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.exception.BusinessException;
import common.exception.ErrorCode;
import feign.RetryableException;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.domain.model.OrderType;
import io.antcamp.tradeservice.domain.model.Trade;
import io.antcamp.tradeservice.domain.model.TradeStatus;
import io.antcamp.tradeservice.domain.model.TradeType;
import io.antcamp.tradeservice.domain.repository.TradeRepository;
import io.antcamp.tradeservice.infrastructure.client.AssetClient;
import io.antcamp.tradeservice.infrastructure.client.KisClient;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.infrastructure.dto.ApprovalTokenRequest;
import io.antcamp.tradeservice.infrastructure.event.producer.TradeEventProducer;
import io.antcamp.tradeservice.infrastructure.event.producer.TradeSucceededEvent;
import io.antcamp.tradeservice.presentation.dto.AssetResponse;
import io.antcamp.tradeservice.presentation.dto.AssetSellRequest;
import io.antcamp.tradeservice.presentation.dto.BuyStockResponse;
import io.antcamp.tradeservice.presentation.dto.DailyChartResponse;
import io.antcamp.tradeservice.presentation.dto.MinutePriceOutput2;
import io.antcamp.tradeservice.presentation.dto.MinutePriceRequestHeader;
import io.antcamp.tradeservice.presentation.dto.MinutePriceRequestParam;
import io.antcamp.tradeservice.presentation.dto.MinutePriceResponse;
import io.antcamp.tradeservice.presentation.dto.PendingOrderResponse;
import io.antcamp.tradeservice.presentation.dto.SellStockResponse;
import io.antcamp.tradeservice.presentation.dto.StockList;
import io.antcamp.tradeservice.presentation.dto.StockPriceList;
import io.antcamp.tradeservice.presentation.dto.TradeOrderRequest;
import io.antcamp.tradeservice.presentation.dto.TradeOrderResponse;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final StringRedisTemplate redisTemplate;
    private final KisClient kisClient;
    private final TradeEventProducer kafkaProducer;
    private final AssetClient assetClient;
    private final ObjectMapper objectMapper;
    private final TradeRepository tradeRepository;

    @Value("${kis.app.key}")
    private String appKey;
    @Value("${kis.app.secret}")
    private String secretKey;
    @Value("${spring.data.redis.timeout:86400}")
    private int timeout;

    private static final String ACCESS_TOKEN_KEY = "kis:access-token";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String CACHE_DAILY_CHART = "kis:chart:";
    private static final String CACHE_MINUTE_PRICE = "kis:price:";

    // ── 토큰 ──────────────────────────────────────────────────────────────

    @Override
    public AccessTokenResponse requestAccessToken() {
        String cachedToken = getAccessToken();
        if (cachedToken != null && !cachedToken.isEmpty()) {
            return new AccessTokenResponse(cachedToken, null, null, null);
        }
        AccessTokenRequest request = new AccessTokenRequest(GRANT_TYPE, appKey, secretKey);
        AccessTokenResponse token = kisClient.requestAccessToken(request);
        if (token.accessToken() != null && !token.accessToken().isEmpty()) {
            saveAccessToken(token.accessToken(), timeout);
        }
        return token;
    }

    @Override
    public String requestApprovalKey() {
        ApprovalTokenRequest request = new ApprovalTokenRequest(GRANT_TYPE, appKey, secretKey);
        return kisClient.requestApprovalKey(request).approvalKey();
    }

    // ── 가격 조회 ──────────────────────────────────────────────────────────

    @Override
    @Retry(name = "kisApi")
    public double getMinutePrice(String stockCode, LocalDateTime dateTime) {
        try {
            MinutePriceResponse kisPrice = getKisPrice(stockCode, dateTime);
            return Double.parseDouble(kisPrice.minutePriceOutput2().get(0).priceNow());
        } catch (RetryableException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.KIS_SERVER_ERROR);
        }
    }

    @Override
    @Retry(name = "kisApi")
    public double getNowPrice(String stockCode, LocalDateTime dateTime) {
        try {
            MinutePriceResponse kisPrice = getKisPrice(stockCode, dateTime);
            return Double.parseDouble(kisPrice.minutePriceOutput1().priceNow());
        } catch (RetryableException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.KIS_SERVER_ERROR);
        }
    }

    @Override
    @Retry(name = "kisApi")
    public MinutePriceResponse getPrice(final String stockCode, final LocalDateTime dateTime) {
        return getKisPrice(stockCode, dateTime);
    }

    @Override
    public StockPriceList stockPriceList(StockList stockList, LocalDateTime dateTime) {
        Map<String, String> stockMap = new HashMap<>();
        for (String stockCode : stockList.stockList()) {
            String price = redisTemplate.opsForValue().get(stockCode);
            if (price == null) {
                price = String.valueOf(getMinutePrice(stockCode, dateTime));
                redisTemplate.opsForValue().set(stockCode, price, Duration.ofSeconds(60));
            }
            stockMap.put(stockCode, price);
        }
        return new StockPriceList(stockMap);
    }

    // ── 주문 (통합) ────────────────────────────────────────────────────────

    /**
     * 시장가 / 지정가 통합 주문. MARKET  → 현재가 즉시 체결 LIMIT   → 조건 충족 시 즉시 체결 / 미충족 시 PENDING 저장
     */
    @Override
    @Transactional
    public TradeOrderResponse placeOrder(TradeOrderRequest request) {
        validateOrderRequest(request);

        LocalDateTime now = LocalDateTime.now();
        MinutePriceResponse priceResponse = getKisPrice(request.stockCode(), now);
        String stockName = priceResponse.minutePriceOutput1().stockName();
        double currentPrice = Double.parseDouble(priceResponse.minutePriceOutput1().priceNow());

        OrderType orderType = request.isMarket() ? OrderType.MARKET : OrderType.LIMIT;
        TradeType tradeType = request.side().equalsIgnoreCase("BUY") ? TradeType.BUY : TradeType.SELL;

        double execPrice = request.isMarket() ? currentPrice : request.limitPrice();
        double totalPrice = currentPrice * request.stockAmount(); // 체결가 기준

        // 지정가 — 조건 미충족 시 PENDING 저장
        if (request.isLimit()) {
            Trade pendingTrade = Trade.create(
                    null, request.accountId(), tradeType, now,
                    request.stockCode(), request.stockAmount(),
                    request.limitPrice() * request.stockAmount(),
                    OrderType.LIMIT, request.limitPrice()
            );

            boolean conditionMet = tradeType == TradeType.BUY
                    ? currentPrice <= request.limitPrice()
                    : currentPrice >= request.limitPrice();

            if (!conditionMet) {
                tradeRepository.save(pendingTrade);
                log.info("[주문] 지정가 미체결 저장 — tradeId={} {} {} {}주 지정가={} 현재가={}",
                        pendingTrade.tradeId(), tradeType, request.stockCode(),
                        request.stockAmount(), request.limitPrice(), currentPrice);
                return TradeOrderResponse.pending(
                        request.stockCode(), stockName, "LIMIT", tradeType.name(),
                        currentPrice, request.limitPrice(), request.stockAmount(),
                        pendingTrade.tradeId()
                );
            }

            // 조건 충족 — 즉시 체결
            totalPrice = currentPrice * request.stockAmount();
            Trade savedTrade = Trade.create(
                    null, request.accountId(), tradeType, now,
                    request.stockCode(), request.stockAmount(), totalPrice,
                    OrderType.LIMIT, request.limitPrice()
            );
            tradeRepository.save(savedTrade);
            executeAsset(savedTrade, currentPrice);
            log.info("[주문] 지정가 즉시체결 — tradeId={} {} {}주 체결가={}",
                    savedTrade.tradeId(), tradeType, request.stockAmount(), currentPrice);
            return TradeOrderResponse.executed(
                    request.stockCode(), stockName, "LIMIT", tradeType.name(),
                    currentPrice, request.stockAmount()
            );
        }

        // 시장가 — 즉시 체결
        Trade newTrade = Trade.create(null, request.accountId(), tradeType, now,
                request.stockCode(), request.stockAmount(), totalPrice,
                OrderType.MARKET, null
        );
        Trade savedTrade = tradeRepository.save(newTrade);
        executeAsset(savedTrade, currentPrice);
        log.info("[주문] 시장가 체결 — tradeId={} {} {}주 체결가={}",
                savedTrade.tradeId(), tradeType, request.stockAmount(), currentPrice);
        return TradeOrderResponse.executed(
                request.stockCode(), stockName, "MARKET", tradeType.name(),
                currentPrice, request.stockAmount()
        );
    }

    /**
     * 미체결 지정가 주문 취소. PENDING 상태인 경우에만 취소 가능하며, 본인 주문인지 검증.
     */
    @Override
    @Transactional
    public TradeOrderResponse cancelOrder(UUID tradeId, UUID accountId) {
        Trade trade = tradeRepository.findById(tradeId);

        if (!trade.accountId().equals(accountId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (trade.tradeStatus() != TradeStatus.PENDING) {
            throw new BusinessException(ErrorCode.TRADE_ALREADY_PROCESSED);
        }

        Trade cancelled = Trade.updateCancelled(trade);
        tradeRepository.updateStatus(cancelled);

        log.info("[주문취소] tradeId={} accountId={}", tradeId, accountId);
        return TradeOrderResponse.cancelled(
                trade.stockCode(), trade.tradeType().name(),
                trade.limitPrice(), trade.stockAmount(), tradeId
        );
    }

    /**
     * 내 미체결 주문 목록
     */
    @Override
    public List<PendingOrderResponse> getPendingOrders(UUID accountId) {
        return tradeRepository.findPendingOrdersByAccountId(accountId)
                .stream()
                .map(PendingOrderResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 스케줄러 전용: PENDING 지정가 주문을 현재가와 비교해 체결. LimitOrderScheduler 에서 @Scheduled 로 호출.
     */
    @Override
    @Transactional
    public void executePendingLimitOrders() {
        List<Trade> pendingOrders = tradeRepository.findPendingLimitOrders();
        if (pendingOrders.isEmpty()) {
            return;
        }

        log.debug("[스케줄러] 미체결 지정가 주문 {}건 처리 시작", pendingOrders.size());

        for (Trade order : pendingOrders) {
            try {
                double currentPrice = getNowPriceCached(order.stockCode());
                if (!order.isLimitConditionMet(currentPrice)) {
                    continue;
                }

                // 체결 처리
                Trade success = Trade.updateSuccess(order);
                tradeRepository.updateStatus(success);
                executeAsset(order, currentPrice);

                log.info("[스케줄러] 지정가 체결 — tradeId={} {} {}주 지정가={} 현재가={}",
                        order.tradeId(), order.tradeType(), order.stockAmount(),
                        order.limitPrice(), currentPrice);

            } catch (Exception e) {
                log.error("[스케줄러] 체결 처리 실패 — tradeId={} error={}", order.tradeId(), e.getMessage());
            }
        }
    }

    // ── 레거시 ────────────────────────────────────────────────────────────

    @Override
    public BuyStockResponse buyStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId) {
        MinutePriceResponse response = getKisPrice(stockCode, dateTime);
        String stockName = response.minutePriceOutput1().stockName();
        double nowPrice = Double.parseDouble(response.minutePriceOutput1().priceNow());
        double totalPrice = nowPrice * stockAmount;

        UUID newTradeId = UUID.randomUUID();
        Trade newTrade = Trade.create(newTradeId, accountId, TradeType.BUY,
                LocalDateTime.now(), stockCode, stockAmount, totalPrice, OrderType.MARKET, null);
        tradeRepository.save(newTrade);

        try {
            AssetResponse assetResponse = assetClient.getAsset(accountId, totalPrice);
            if (assetResponse.tradeAt() != null) {
                tradeRepository.updateStatus(Trade.updateSuccess(tradeRepository.findById(newTradeId)));
            }
        } catch (Exception e) {
            log.error("asset server 응답 실패");
        }
        return new BuyStockResponse(stockCode, stockName, totalPrice, stockAmount);
    }

    @Override
    public SellStockResponse sellStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId) {
        MinutePriceResponse response = getKisPrice(stockCode, dateTime);
        String stockName = response.minutePriceOutput1().stockName();
        double nowPrice = Double.parseDouble(response.minutePriceOutput1().priceNow());
        double totalPrice = nowPrice * stockAmount;

        UUID newTradeId = UUID.randomUUID();
        Trade newTrade = Trade.create(newTradeId, accountId, TradeType.SELL,
                LocalDateTime.now(), stockCode, stockAmount, totalPrice, OrderType.MARKET, null);
        tradeRepository.save(newTrade);

        try {
            AssetSellRequest sellRequest = new AssetSellRequest(accountId, stockCode, stockAmount, nowPrice);
            AssetResponse assetResponse = assetClient.getStock(sellRequest);
            if (assetResponse.tradeAt() != null) {
                tradeRepository.updateStatus(Trade.updateSuccess(tradeRepository.findById(newTradeId)));
            }
        } catch (Exception e) {
            log.error("asset server 응답 실패");
        }
        return new SellStockResponse(stockCode, stockName, totalPrice, stockAmount);
    }

    // ── 일/주/월/년봉 ──────────────────────────────────────────────────────

    @Override
    @Retry(name = "kisApi")
    public DailyChartResponse getDailyChart(String stockCode, String startDate, String endDate, String periodDivCode) {
        String cacheKey = CACHE_DAILY_CHART + stockCode + ":" + periodDivCode + ":" + startDate + ":" + endDate;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                log.debug("일/주/월봉 캐시 히트: {}", cacheKey);
                return objectMapper.readValue(cached, DailyChartResponse.class);
            } catch (JsonProcessingException ignored) {
            }
        }
        try {
            Map<String, Object> header = buildCommonHeader("FHKST03010100");
            Map<String, Object> param = Map.of(
                    "FID_COND_MRKT_DIV_CODE", "J",
                    "FID_INPUT_ISCD", stockCode,
                    "FID_INPUT_DATE_1", startDate,
                    "FID_INPUT_DATE_2", endDate,
                    "FID_PERIOD_DIV_CODE", periodDivCode,
                    "FID_ORG_ADJ_PRC", "0"
            );
            log.info("일/주/월봉 KIS 호출 — stockCode={} period={} start={} end={}",
                    stockCode, periodDivCode, startDate, endDate);
            String raw = kisClient.getDailyChart(header, param);
            DailyChartResponse response = objectMapper.readValue(raw, DailyChartResponse.class);
            if (!"0".equals(response.rtCd())) {
                throw new BusinessException(ErrorCode.KIS_SERVER_ERROR);
            }
            Duration ttl = "D".equals(periodDivCode) ? Duration.ofMinutes(5) : Duration.ofMinutes(30);
            redisTemplate.opsForValue().set(cacheKey, raw, ttl);
            return response;
        } catch (RetryableException e) {
            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("일/주/월봉 조회 실패 stockCode={}: {}", stockCode, e.getMessage());
            throw new BusinessException(ErrorCode.KIS_SERVER_ERROR);
        }
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────────

    /**
     * AssetClient 호출 후 Trade 상태 업데이트 + Kafka 발행. 매수 / 매도 분기 처리.
     */
    private void executeAsset(Trade trade, double executedPrice) {
        try {
            if (trade.tradeType() == TradeType.BUY) {
                assetClient.getAsset(trade.accountId(), trade.totalPrice());
            } else {
                assetClient.getStock(new AssetSellRequest(
                        trade.accountId(), trade.stockCode(),
                        trade.stockAmount(), executedPrice));
            }
            Trade success = Trade.updateSuccess(trade);
            tradeRepository.updateStatus(success);
            kafkaProducer.publishTradeResult(
                    new TradeSucceededEvent(trade.totalPrice(), trade.accountId(), trade.tradeId())
            );
        } catch (Exception e) {
            log.error("[체결 오류] tradeId={} error={}", trade.tradeId(), e.getMessage());
            tradeRepository.updateStatus(Trade.updateFail(trade));
        }
    }

    /**
     * 스케줄러에서 현재가 조회 시 Redis 캐시 우선 사용. 캐시 미스 시 KIS API 호출.
     */
    private double getNowPriceCached(String stockCode) {
        String cached = redisTemplate.opsForValue().get(stockCode);
        if (cached != null) {
            return Double.parseDouble(cached);
        }
        return getNowPrice(stockCode, LocalDateTime.now());
    }

    private void validateOrderRequest(TradeOrderRequest request) {
        if (request.isLimit() && request.limitPrice() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.limitPrice() != null && request.limitPrice() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.stockAmount() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private MinutePriceResponse getKisPrice(String stockCode, LocalDateTime dateTime) {
        String date = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = dateTime.format(DateTimeFormatter.ofPattern("HHmmss"));

        String cacheKey = CACHE_MINUTE_PRICE + stockCode + ":" + date;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                log.debug("분봉 캐시 히트: {}", cacheKey);
                MinutePriceResponse cachedResponse = objectMapper.readValue(cached, MinutePriceResponse.class);
                return filterFutureCandles(cachedResponse, dateTime);
            } catch (JsonProcessingException ignored) {
            }
        }

        Map<String, Object> header = MinutePriceRequestHeader.create(
                "Bearer " + requestAccessToken().accessToken(), appKey, secretKey);
        Map<String, Object> param = MinutePriceRequestParam.create(stockCode, time, date);

        log.info("KIS 분봉 호출 stockCode={} date={} time={}", stockCode, date, time);
        String response = kisClient.getMinutePrice(header, param);
        try {
            MinutePriceResponse parsed = objectMapper.readValue(response, MinutePriceResponse.class);
            if (!"0".equals(parsed.rtCd())) {
                log.error("분봉 KIS 에러 응답 stockCode={} rt_cd={} msg={}",
                        stockCode, parsed.rtCd(), parsed.responseMessage());
                throw new BusinessException(ErrorCode.KIS_SERVER_ERROR);
            }
            MinutePriceResponse filtered = filterFutureCandles(parsed, dateTime);
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofSeconds(30));
            return filtered;
        } catch (BusinessException e) {
            throw e;
        } catch (JsonProcessingException e) {
            log.error("분봉 응답 파싱 실패 stockCode={}", stockCode);
            throw new RuntimeException(e);
        }
    }

    private static final DateTimeFormatter KIS_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private MinutePriceResponse filterFutureCandles(MinutePriceResponse response, LocalDateTime queryTime) {
        if (response.minutePriceOutput2() == null || response.minutePriceOutput2().isEmpty()) {
            return response;
        }
        List<MinutePriceOutput2> filtered = response.minutePriceOutput2().stream()
                .filter(candle -> {
                    try {
                        LocalDate d = LocalDate.parse(candle.date(), KIS_DATE_FMT);
                        int h = Integer.parseInt(candle.time().substring(0, 2));
                        int m = Integer.parseInt(candle.time().substring(2, 4));
                        int s = Integer.parseInt(candle.time().substring(4, 6));
                        return !d.atTime(h, m, s).isAfter(queryTime);
                    } catch (Exception e) {
                        return true;
                    }
                })
                .collect(Collectors.toList());

        int removed = response.minutePriceOutput2().size() - filtered.size();
        if (removed > 0) {
            log.debug("미래 캔들 {}개 제거 (쿼리시각={})", removed, queryTime);
        }

        return new MinutePriceResponse(
                response.minutePriceOutput1(), filtered,
                response.rtCd(), response.responseCode(), response.responseMessage()
        );
    }

    private Map<String, Object> buildCommonHeader(String trId) {
        return Map.of(
                "content-type", "application/json; charset=utf-8",
                "authorization", "Bearer " + requestAccessToken().accessToken(),
                "appkey", appKey,
                "appsecret", secretKey,
                "tr_id", trId,
                "custtype", "P",
                "tr_cont", "N"
        );
    }

    @Override
    public void clearAll() {
        redisTemplate.delete(ACCESS_TOKEN_KEY);
    }

    private void saveAccessToken(String token, long expiresInSeconds) {
        redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, token, Duration.ofHours(23));
    }

    private String getAccessToken() {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
    }
}
