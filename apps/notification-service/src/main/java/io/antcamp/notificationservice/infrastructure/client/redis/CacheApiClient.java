package io.antcamp.notificationservice.infrastructure.client.redis;

import io.antcamp.notificationservice.application.port.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheApiClient implements CachePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void clear(String job) {
        String suffix = stripJobPrefix(job);
        if (suffix.isBlank()) {
            throw new IllegalArgumentException("캐시 패턴 생성 실패 (빈 suffix): job=" + job);
        }
        String pattern = suffix + "*";

        List<String> keys = redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> result = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                cursor.forEachRemaining(key -> result.add(new String(key, StandardCharsets.UTF_8)));
            }
            return result;
        });

        if (keys == null || keys.isEmpty()) {
            log.info("삭제할 캐시 키 없음: pattern={}", pattern);
            return;
        }

        Long deleted = redisTemplate.delete(keys);
        log.info("캐시 삭제 완료: pattern={}, deleted={}건", pattern, deleted);
    }

    private String stripJobPrefix(String job) {
        if (job == null) return "";
        return job.startsWith("antcamp-") ? job.substring("antcamp-".length()) : job;
    }
}