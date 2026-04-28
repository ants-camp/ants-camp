package io.antcamp.rankingservice.infrastructure.messaging.kafka;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * application.yml의 topics.ranking.* 설정을 매핑한다. 토픽명을 코드에 하드코딩하지 않기 위함.
 */
@Validated
@ConfigurationProperties(prefix = "topics.ranking")
public record RankingTopicProperties(
        @NotBlank String assetUpdated,
        @NotBlank String participantsValuated
) {
}
