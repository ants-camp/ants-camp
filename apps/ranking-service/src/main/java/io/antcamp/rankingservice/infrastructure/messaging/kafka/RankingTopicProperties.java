package io.antcamp.rankingservice.infrastructure.messaging.kafka;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 랭킹 서비스가 수신하는 Kafka 토픽명을 관리한다.
 * - topics.trade.*  : 매매 서비스(trade-service)로부터 수신
 * - topics.asset.*  : 자산 서비스(asset-service)로부터 수신
 */
@Validated
@ConfigurationProperties(prefix = "topics")
public record RankingTopicProperties(
        Trade trade,
        Asset asset
) {
    public record Trade(
            @NotBlank String tradeSucceeded,        // 매매 체결 시 단건 실시간 순위 갱신
            @NotBlank String rankingUpdateRequested  // 1분마다 대회 참가자 전체 총자산 일괄 갱신
    ) {}

    public record Asset(
            @NotBlank String totalCalculated   // 대회 종료 후 최종 총자산 수신 → 최종 순위 DB 저장
    ) {}
}
