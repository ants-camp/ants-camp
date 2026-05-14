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
import io.antcamp.tradeservice.presentation.dto.AssetResponse;
import io.antcamp.tradeservice.presentation.dto.AssetSellRequest;
import io.antcamp.tradeservice.presentation.dto.BuyHoldingRequest;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final StringRedisTemplate redisTemplate;
    private final KisClient kisClient;
    private final AssetClient assetClient;
    private final ObjectMapper objectMapper;
    private final TradeRepository tradeRepository;
    /**
     * Self-injection — REQUIRES_NEW 트랜잭션 분리를 위해 proxy 경유 호출이 필요.
     * 직접 this.executePendingLimitOrder(...) 로 호출하면 self-invocation 이라
     * Spring AOP proxy 가 적용되지 않아 새 트랜잭션이 열리지 않는다.
     *
     * 수정 전: @Lazy private final TradeService self;
     *   → Lombok @RequiredArgsConstructor 가 필드의 @Lazy 를 생성자 파라미터로 전파하지 않아
     *     일반 의존성으로 해석돼 LimitOrderScheduler ↔ TradeServiceImpl 순환참조 발생.
     * 수정 후: ObjectProvider 로 lazy lookup. 호출 시점에 빈을 가져오므로 순환 안 생김.
     */
    private final ObjectProvider<TradeService> selfProvider;

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
     *
     * <p>접수 시점 사전 검증 (수정 후 추가):
     * <ul>
     *   <li>SELL — asset-service 보유 수량 확인, 부족 시 즉시 거부 (phantom PENDING 차단)</li>
     *   <li>BUY  — asset-service 현금 잔액 확인, 부족 시 즉시 거부</li>
     * </ul>
     * 한계: 동일 종목으로 동시 PENDING 여러 건 들어오면 합계 기준 검증은 못 함 (예약 락이 없음).
     * 모의투자 트래픽 수준에선 무시 가능. 후속으로 reservation 패턴 도입 권장.
     */
    // 수정 전: public TradeOrderResponse placeOrder(TradeOrderRequest request)
    @Override
    @Transactional
    public TradeOrderResponse placeOrder(TradeOrderRequest request, UUID userId) {
        validateOrderRequest(request);

        LocalDateTime now = LocalDateTime.now();
        MinutePriceResponse priceResponse = getKisPrice(request.stockCode(), now);
        String stockName = priceResponse.minutePriceOutput1().stockName();
        double currentPrice = Double.parseDouble(priceResponse.minutePriceOutput1().priceNow());

        OrderType orderType = request.isMarket() ? OrderType.MARKET : OrderType.LIMIT;
        TradeType tradeType = request.side().equalsIgnoreCase("BUY") ? TradeType.BUY : TradeType.SELL;

        double execPrice = request.isMarket() ? currentPrice : request.limitPrice();
        double totalPrice = currentPrice * request.stockAmount(); // 체결가 기준

        // 수정 후 신규: 접수 시점 사전 검증 (SELL=보유, BUY=잔액)
        // 시장가/지정가/즉시체결/PENDING 모든 분기 공통 적용 — phantom PENDING 차단 목적.
        validateSufficientFundsOrHoldings(request, tradeType, currentPrice);

        // 지정가 — 조건 미충족 시 PENDING 저장
        if (request.isLimit()) {
            boolean conditionMet = tradeType == TradeType.BUY
                    ? currentPrice <= request.limitPrice()
                    : currentPrice >= request.limitPrice();

            if (!conditionMet) {
                Trade pendingTrade = Trade.create(
                        null, request.accountId(), userId, tradeType, now,
                        request.stockCode(), request.stockAmount(),
                        request.limitPrice() * request.stockAmount(),
                        OrderType.LIMIT, request.limitPrice()
                );
                // 수정 전: tradeRepository.save(pendingTrade); → 반환된 tradeId 가 유실되어 응답·취소 시 null 사용됨
                Trade savedPending = tradeRepository.save(pendingTrade);
                log.info("[주문] 지정가 미체결 저장 — tradeId={} {} {} {}주 지정가={} 현재가={}",
                        savedPending.tradeId(), tradeType, request.stockCode(),
                        request.stockAmount(), request.limitPrice(), currentPrice);
                return TradeOrderResponse.pending(
                        request.stockCode(), stockName, "LIMIT", tradeType.name(),
                        currentPrice, request.limitPrice(), request.stockAmount(),
                        savedPending.tradeId()
                );
            }

            // 조건 충족 — 즉시 체결
            totalPrice = currentPrice * request.stockAmount();
            Trade newLimitTrade = Trade.create(
                    null, request.accountId(), userId, tradeType, now,
                    request.stockCode(), request.stockAmount(), totalPrice,
                    OrderType.LIMIT, request.limitPrice()
            );
            // 수정 전: tradeRepository.save(savedTrade);  ← 반환값을 무시해 savedTrade.tradeId() 가 null →
            //         executeAsset 내부 updateStatus → findById(null) → 500 발생
            Trade savedTrade = tradeRepository.save(newLimitTrade);
            try {
                executeAsset(savedTrade, currentPrice, userId);
            } catch (Exception e) {
                log.error("[주문] 지정가 즉시체결 실패 — tradeId={} error={}", savedTrade.tradeId(), e.getMessage());
                tradeRepository.updateStatus(Trade.updateFail(savedTrade));
                return TradeOrderResponse.failed(
                        request.stockCode(), stockName, "LIMIT", tradeType.name(),
                        currentPrice, request.stockAmount()
                );
            }
            log.info("[주문] 지정가 즉시체결 — tradeId={} {} {}주 체결가={}",
                    savedTrade.tradeId(), tradeType, request.stockAmount(), currentPrice);
            return TradeOrderResponse.executed(
                    request.stockCode(), stockName, "LIMIT", tradeType.name(),
                    currentPrice, request.stockAmount()
            );
        }

        // 시장가 — 즉시 체결
        Trade newTrade = Trade.create(null, request.accountId(), userId, tradeType, now,
                request.stockCode(), request.stockAmount(), totalPrice,
                OrderType.MARKET, null
        );
        Trade savedTrade = tradeRepository.save(newTrade);
        // 수정 전: executeAsset(savedTrade, currentPrice);
        try {
            executeAsset(savedTrade, currentPrice, userId);
        } catch (Exception e) {
            log.error("[주문] 시장가 체결 실패 — tradeId={} error={}", savedTrade.tradeId(), e.getMessage());
            tradeRepository.updateStatus(Trade.updateFail(savedTrade));
            return TradeOrderResponse.failed(
                    request.stockCode(), stockName, "MARKET", tradeType.name(),
                    currentPrice, request.stockAmount()
            );
        }
        log.info("[주문] 시장가 체결 — tradeId={} {} {}주 체결가={}",
                savedTrade.tradeId(), tradeType, request.stockAmount(), currentPrice);
        return TradeOrderResponse.executed(
                request.stockCode(), stockName, "MARKET", tradeType.name(),
                currentPrice, request.stockAmount()
        );
    }

    /**
     * 접수 시점 사전 검증.
     * BUY: 필요 현금(= 체결예상가 × 수량) ≤ 현재 잔액
     * SELL: 요청 수량 ≤ 현재 보유 수량
     *
     * 검증 가격 — LIMIT BUY 는 보수적으로 max(currentPrice, limitPrice) 를 쓰지 않고
     * 의도된 한도가인 limitPrice 기준으로 검증한다 (실제 체결가는 ≤ limitPrice 이므로 안전).
     * SELL 은 가격과 무관하게 수량만 검증.
     */
    private void validateSufficientFundsOrHoldings(TradeOrderRequest request,
                                                    TradeType tradeType,
                                                    double currentPrice) {
        if (tradeType == TradeType.SELL) {
            int held;
            try {
                held = assetClient.getHoldingQuantity(request.accountId(), request.stockCode());
            } catch (Exception e) {
                log.error("[주문] 보유 조회 실패 — accountId={} stockCode={} error={}",
                        request.accountId(), request.stockCode(), e.getMessage());
                throw new BusinessException(ErrorCode.ASSET_SERVICE_ERROR);
            }
            if (held < request.stockAmount()) {
                log.info("[주문] 보유 부족 거부 — accountId={} stockCode={} 보유={} 요청={}",
                        request.accountId(), request.stockCode(), held, request.stockAmount());
                throw new BusinessException(ErrorCode.INSUFFICIENT_HOLDINGS);
            }
            return;
        }

        // BUY
        double priceForCheck = request.isLimit() ? request.limitPrice() : currentPrice;
        long requiredCash = (long) (priceForCheck * request.stockAmount());
        long balance;
        try {
            balance = assetClient.getAccountBalance(request.accountId());
        } catch (Exception e) {
            log.error("[주문] 잔액 조회 실패 — accountId={} error={}",
                    request.accountId(), e.getMessage());
            throw new BusinessException(ErrorCode.ASSET_SERVICE_ERROR);
        }
        if (balance < requiredCash) {
            log.info("[주문] 잔액 부족 거부 — accountId={} 잔액={} 필요={}",
                    request.accountId(), balance, requiredCash);
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
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
     *
     * <p>수정 전 버그 (두 가지):
     * <ol>
     *   <li>userId 를 null 로 전달해 asset-service 가 X-User-Id 헤더 누락으로 400 반환.</li>
     *   <li>executeAsset 호출 전에 updateSuccess 를 먼저 in-memory 마킹 → asset 호출 실패해도
     *       catch 가 트랜잭션을 롤백시키지 않아 SUCCESS 가 그대로 flush 되는 데이터 정합성 깨짐.</li>
     * </ol>
     *
     * <p>수정 후:
     * <ul>
     *   <li>주문 시점에 보존된 trade.userId() 를 사용</li>
     *   <li>executeAsset 호출 (내부에서 성공 시에만 updateSuccess) → 실패 시 catch 에서 updateFail</li>
     *   <li>한 건 실패가 다른 건 처리를 막지 않도록 각 iteration 을 별도 트랜잭션으로 분리
     *       (self-invocation 회피용 SELF 빈 참조)</li>
     * </ul>
     */
    @Override
    public void executePendingLimitOrders() {
        List<Trade> pendingOrders = tradeRepository.findPendingLimitOrders();
        if (pendingOrders.isEmpty()) {
            return;
        }

        log.info("[스케줄러] 미체결 지정가 주문 {}건 처리 시작", pendingOrders.size());

        for (Trade order : pendingOrders) {
            try {
                double currentPrice = getNowPriceCached(order.stockCode());
                if (!order.isLimitConditionMet(currentPrice)) {
                    continue;
                }
                // self proxy 호출로 REQUIRES_NEW 트랜잭션 시작 — 건별 독립 커밋/롤백
                // 수정 전: self.executePendingLimitOrder(...) → 순환참조 (위 selfProvider 주석 참조)
                selfProvider.getObject().executePendingLimitOrder(order, currentPrice);
            } catch (Exception e) {
                log.error("[스케줄러] 체결 처리 실패 — tradeId={} error={}",
                        order.tradeId(), e.getMessage());
            }
        }
    }

    /**
     * PENDING 1건 체결 처리 — 건별 독립 트랜잭션.
     * <p>asset-service 호출 성공 시에만 trade 를 SUCCESS 로, 실패 시 FAIL 로 마킹.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executePendingLimitOrder(Trade order, double currentPrice) {
        try {
            // 수정 전: 여기서 updateSuccess 를 먼저 호출했음 → 아래 executeAsset 실패해도
            //          @Transactional 이 catch 로 인해 롤백 안 되고 SUCCESS 가 commit 되던 버그.
            // executeAsset 내부에서 asset 호출 성공 후에만 updateSuccess 호출하도록 위임.
            executeAsset(order, currentPrice, order.userId());
            log.info("[스케줄러] 지정가 체결 — tradeId={} {} {}주 지정가={} 현재가={}",
                    order.tradeId(), order.tradeType(), order.stockAmount(),
                    order.limitPrice(), currentPrice);
        } catch (Exception e) {
            // 자산 호출 실패 시 FAIL 로 마킹하고 정상 종료해야 트랜잭션이 커밋되어 상태가 persist 된다.
            // throw 로 재던지면 @Transactional 이 롤백하면서 updateFail 도 같이 사라져 PENDING 좀비가 생김.
            // 호출자(executePendingLimitOrders) 는 이 메서드의 성공/실패를 별도로 알 필요가 없으므로 로깅으로 충분.
            log.error("[스케줄러] asset 호출 실패 → FAIL 처리 — tradeId={} userId={} error={}",
                    order.tradeId(), order.userId(), e.getMessage());
            tradeRepository.updateStatus(Trade.updateFail(order));
        }
    }

    // ── 레거시 ────────────────────────────────────────────────────────────

    // 수정 전: buyStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId)
    @Override
    public BuyStockResponse buyStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId, UUID userId) {
        MinutePriceResponse response = getKisPrice(stockCode, dateTime);
        String stockName = response.minutePriceOutput1().stockName();
        double nowPrice = Double.parseDouble(response.minutePriceOutput1().priceNow());
        double totalPrice = nowPrice * stockAmount;

        UUID newTradeId = UUID.randomUUID();
        // 수정 전: Trade.create(newTradeId, accountId, TradeType.BUY, ...) — userId 인자 추가
        Trade newTrade = Trade.create(newTradeId, accountId, userId, TradeType.BUY,
                LocalDateTime.now(), stockCode, stockAmount, totalPrice, OrderType.MARKET, null);
        tradeRepository.save(newTrade);

        try {
            // 수정 전: assetClient.getAsset(accountId, totalPrice);
            assetClient.getAsset(userId, new BuyHoldingRequest(accountId, stockCode, stockAmount, (long) nowPrice));
            tradeRepository.updateStatus(Trade.updateSuccess(tradeRepository.findById(newTradeId)));
        } catch (Exception e) {
            log.error("asset server 응답 실패");
        }
        return new BuyStockResponse(stockCode, stockName, totalPrice, stockAmount);
    }

    // 수정 전: sellStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId)
    @Override
    public SellStockResponse sellStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId, UUID userId) {
        MinutePriceResponse response = getKisPrice(stockCode, dateTime);
        String stockName = response.minutePriceOutput1().stockName();
        double nowPrice = Double.parseDouble(response.minutePriceOutput1().priceNow());
        double totalPrice = nowPrice * stockAmount;

        UUID newTradeId = UUID.randomUUID();
        // 수정 전: Trade.create(newTradeId, accountId, TradeType.SELL, ...) — userId 인자 추가
        Trade newTrade = Trade.create(newTradeId, accountId, userId, TradeType.SELL,
                LocalDateTime.now(), stockCode, stockAmount, totalPrice, OrderType.MARKET, null);
        tradeRepository.save(newTrade);

        try {
            // 수정 전: new AssetSellRequest(accountId, stockCode, stockAmount, nowPrice)  ← double을 그대로 넘김
            AssetSellRequest sellRequest = new AssetSellRequest(accountId, stockCode, stockAmount, (long) nowPrice);
            // 수정 전: assetClient.getStock(sellRequest);
            assetClient.getStock(userId, sellRequest);
            tradeRepository.updateStatus(Trade.updateSuccess(tradeRepository.findById(newTradeId)));
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
     * 실패 시 예외를 호출부로 전파 (호출부에서 Trade FAIL 처리 및 응답 결정)
     */
    // 수정 전: private void executeAsset(Trade trade, double executedPrice)
    private void executeAsset(Trade trade, double executedPrice, UUID userId) {
        // 수정 전:
        // if (trade.tradeType() == TradeType.BUY) {
        //     assetClient.getAsset(trade.accountId(), trade.totalPrice());
        // } else {
        //     assetClient.getStock(new AssetSellRequest(
        //             trade.accountId(), trade.stockCode(),
        //             trade.stockAmount(), executedPrice));
        // }
        if (trade.tradeType() == TradeType.BUY) {
            assetClient.getAsset(userId, new BuyHoldingRequest(
                    trade.accountId(),
                    trade.stockCode(),
                    trade.stockAmount(),
                    (long) executedPrice   // 주당 가격
            ));
        } else {
            // 수정 전: new AssetSellRequest(..., executedPrice)  ← double을 그대로 넘김
            assetClient.getStock(userId, new AssetSellRequest(
                    trade.accountId(), trade.stockCode(),
                    trade.stockAmount(), (long) executedPrice));
        }
        Trade success = Trade.updateSuccess(trade);
        tradeRepository.updateStatus(success);
        // 수정 전: catch 블록으로 예외를 삼키던 코드 제거
        // catch (Exception e) {
        //     log.error("[체결 오류] tradeId={} error={}", trade.tradeId(), e.getMessage());
        //     tradeRepository.updateStatus(Trade.updateFail(trade));
        // }
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
