package io.antcamp.assistantservice.infrastructure.vector;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * 인제스트 파이프라인 재시도 정책
 */
public final class IngestRetryPolicy {

    /**
     * 재시도 가능한 예외 목록 (일시 장애).
     * 이 목록에 없는 예외는 영구 실패로 분류된다.
     */
    static final Set<Class<? extends Throwable>> RETRYABLE_EXCEPTIONS = Set.of(
            IOException.class,
            HttpServerErrorException.class,
            TransientDataAccessException.class
    );

    private IngestRetryPolicy() {}

    public static String classify(Exception e) {
        boolean retryable = RETRYABLE_EXCEPTIONS.stream().anyMatch(type -> type.isInstance(e));
        return retryable ? "TRANSIENT" : "PERMANENT_INVALID_CONTENT";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Retryable(
            retryFor = {IOException.class, HttpServerErrorException.class, TransientDataAccessException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    public @interface Retry {}
}