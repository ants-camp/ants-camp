package io.antcamp.tradeservice.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.domain.model.Trade;
import io.antcamp.tradeservice.domain.model.TradeType;
import io.antcamp.tradeservice.domain.repository.TradeRepository;
import io.antcamp.tradeservice.infrastructure.client.AssetClient;
import io.antcamp.tradeservice.infrastructure.client.KisClient;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.infrastructure.entity.TradeEntity;
import io.antcamp.tradeservice.infrastructure.event.producer.TradeEventProducer;
import io.antcamp.tradeservice.infrastructure.event.producer.TradeSucceededEvent;
import io.antcamp.tradeservice.infrastructure.exception.KisApiException;
import io.antcamp.tradeservice.infrastructure.repository.TradeJpaRepository;
import io.antcamp.tradeservice.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.ByteChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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

    // ── 토큰 발급 ──────────────────────────────────────────────────────────

    @Override
    public AccessTokenResponse requestAccessToken() {
        String cachedToken = getAccessToken();
        if (cachedToken != null && !cachedToken.isEmpty()) {
            return new AccessTokenResponse(cachedToken,null, null,null );
        }

        AccessTokenRequest request = new AccessTokenRequest(GRANT_TYPE, appKey, secretKey);
        AccessTokenResponse token = kisClient.requestAccessToken(request);  // 실패 시 ErrorDecoder 가 처리

        if (token.accessToken() != null && !token.accessToken().isEmpty()) {
            saveAccessToken(token.accessToken(), timeout);
        }
        return token;
    }

    @Override
    public String requestApprovalKey() {
        AccessTokenRequest request = new AccessTokenRequest(GRANT_TYPE, appKey, secretKey);
        return kisClient.requestApprovalKey(request).approvalKey();
    }

    // ── 분봉 조회 ──────────────────────────────────────────────────────────

    @Override
    public double getMinutePrice(String stockCode, LocalDateTime dateTime)  {
        try{
            MinutePriceResponse kisPrice = getKisPrice(stockCode, dateTime);
            return Double.parseDouble(kisPrice.minutePriceOutput2().get(0).priceNow());
        } catch (Exception e){
            throw new BusinessException(ErrorCode.KIS_SERVER_ERROR);
        }
    }

    private MinutePriceResponse getKisPrice(String stockCode, LocalDateTime dateTime){
        String date = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = dateTime.format(DateTimeFormatter.ofPattern("HHmmss"));

        Map<String ,Object> header = MinutePriceRequestHeader.create(
                "Bearer " + requestAccessToken().accessToken(),
                appKey,
                secretKey
        );
        Map<String ,Object> param = MinutePriceRequestParam.create(stockCode, time, date);

        String response = kisClient.getMinutePrice(header, param);
        try{
            return objectMapper.readValue(response, MinutePriceResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getNowPrice(String stockCode, LocalDateTime dateTime)  {
        try{
            MinutePriceResponse kisPrice = getKisPrice(stockCode, dateTime);
            return Double.parseDouble(kisPrice.minutePriceOutput1().priceNow());
        } catch (Exception e){
            throw new BusinessException(ErrorCode.KIS_SERVER_ERROR);
        }
    }

    @Override
    public BuyStockResponse buyStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId) {

        // 현재 가격 먼저 조회
        MinutePriceResponse response = getKisPrice(stockCode, dateTime);
        String stockName = response.minutePriceOutput1().stockName();
        double nowPrice = Double.parseDouble(response.minutePriceOutput1().priceNow());
        double totalPrice = nowPrice*stockAmount;

        // 매매 데이터 생성 & 저장
        UUID newTradeId = UUID.randomUUID();
        LocalDateTime tradeAt = LocalDateTime.now();
        Trade newTrade = Trade.create(newTradeId, accountId, TradeType.BUY, tradeAt, stockCode, stockAmount, totalPrice);
        tradeRepository.save(newTrade);

        // 자산 서비스에 validation 요청 (매매 데이터 생성하고 자산서비스로 넘겨주는 느낌으로?
        // -> 살때랑 팔때랑 assetservice에서 다른 로직이 필요하자나
        // buy -> 금액충분한지확인, sell -> 홀딩스가 충분한지
        // 자산서비스에서는 canTrade 만 리턴해주고 -> Trade서비스에서는 FAIL, SUCCESS로 상태변경
        // 자산서비스에서는 그냥 validation 되면 redis sorted set에 업데이트)
        try{
            AssetResponse assetResponse = assetClient.getAsset(accountId, totalPrice);
            if(assetResponse.tradeAt()!=null){
                Trade foundTrade = tradeRepository.findById(newTradeId);
                Trade successTrade = Trade.updateSuccess(foundTrade);
                tradeRepository.updateStatus(successTrade);
            }
        }catch (Exception e){
            log.error("asset server 응답 실패");
        }
        return new BuyStockResponse(
                stockCode,
                stockName,
                totalPrice,
                stockAmount
        );
    }

    @Override
    public StockPriceList stockPriceList(StockList stockList, LocalDateTime dateTime) {

        Map<String ,String > stockMap = new HashMap<>();

        // for
        for(String stockCode:stockList.stockList()){
            // redis 에 있는지 먼저 확인
            String price = redisTemplate.opsForValue().get(stockCode);
            if(price==null){
                price = String.valueOf(getMinutePrice(stockCode, dateTime));
                redisTemplate.opsForValue().set(stockCode, price, Duration.ofSeconds(60));
            }
            stockMap.put(stockCode, price);
        }

        return new StockPriceList(stockMap);
    }

    @Override
    public SellStockResponse sellStock(LocalDateTime dateTime, String stockCode, int stockAmount, UUID accountId) {

        // 현재 가격 먼저 조회
        MinutePriceResponse response = getKisPrice(stockCode, dateTime);
        String stockName = response.minutePriceOutput1().stockName();
        double nowPrice = Double.parseDouble(response.minutePriceOutput1().priceNow());
        double totalPrice = nowPrice*stockAmount;

        // 매매 데이터 생성 & 저장
        UUID newTradeId = UUID.randomUUID();
        LocalDateTime tradeAt = LocalDateTime.now();
        Trade newTrade = Trade.create(newTradeId, accountId, TradeType.SELL, tradeAt, stockCode, stockAmount, totalPrice);
        tradeRepository.save(newTrade);

        // 자산 서비스에 validation 요청 & event publish
        try{
            AssetResponse assetResponse = assetClient.getAsset(accountId, totalPrice);
            if(assetResponse.tradeAt()!=null){
                Trade foundTrade = tradeRepository.findById(newTradeId);
                Trade successTrade = Trade.updateSuccess(foundTrade);
                tradeRepository.updateStatus(successTrade);
            }
        }catch (Exception e){
            log.error("asset server 응답 실패");
        }
        return new SellStockResponse(
                stockCode,
                stockName,
                totalPrice,
                stockAmount
        );
    }


    /** 3회 재시도 후 최종 실패 시 폴백 */
    private MinutePriceResponse getMinutePriceFallback(String stockCode,
                                                        LocalDateTime dateTime,
                                                        Exception e) {
        if (e instanceof KisApiException ex) {
            log.error("KIS 분봉 조회 실패 [{}] stockCode={}: {}", ex.getErrorCode(), stockCode, ex.getMessage());
        } else {
            log.error("KIS 분봉 조회 최종 실패 stockCode={}: {}", stockCode, e.getMessage());
        }
        return MinutePriceResponse.empty();  // 빈 응답 반환 (서킷 오픈 대신 graceful degradation)
    }

    // ── 공통 ───────────────────────────────────────────────────────────────

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
