package io.antcamp.notificationservice.infrastructure.client.slack;

import io.antcamp.notificationservice.application.port.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheApiClient implements CachePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void clear(String job) {
        String pattern = job.replace("antcamp-", "") + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys.isEmpty()) {
            log.info("삭제할 캐시 키 없음: pattern={}", pattern);
            return;
        }
        Long deleted = redisTemplate.delete(keys);
        log.info("캐시 삭제 완료: pattern={}, deleted={}건", pattern, deleted);
    }
}
