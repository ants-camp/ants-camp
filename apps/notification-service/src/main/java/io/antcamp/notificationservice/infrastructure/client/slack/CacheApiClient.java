package io.antcamp.notificationservice.infrastructure.client.slack;

import io.antcamp.notificationservice.application.port.CachePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheApiClient implements CachePort {

    @Override
    public void clear(String job) {
        // TODO: 공용 Redis 인프라 도입 후 실제 캐시 초기화 구현
        log.info("캐시 비우기 요청: job={}", job);
    }
}