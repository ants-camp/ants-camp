package io.antcamp.competitionservice.infrastructure.messaging.kafka;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * application.yml의 topics.competition.* 설정을 매핑한다. 토픽명을 코드에 하드코딩하지 않기 위함.
 */
@Validated
@ConfigurationProperties(prefix = "topics.competition")
public record CompetitionTopicProperties(
        @NotBlank String registered,
        @NotBlank String finished,
        @NotBlank String cancelled,
        @NotBlank String aborted,
        @NotBlank String ticked
) {
}
