package io.antcamp.assistantservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@EnableRetry
public class AsyncConfig {

    @Bean(name = "ingestExecutor")
    public Executor ingestExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 코어2
        executor.setMaxPoolSize(5);         // 최대5
        executor.setQueueCapacity(50);      // 대기열 50
        executor.setThreadNamePrefix("ingest-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "evalExecutor")
    public Executor evalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);      // 평가 run 단위로 큐잉 — 각 run이 내부에서 순차 처리
        executor.setThreadNamePrefix("eval-");
        // 큐 포화 시 호출 스레드에서 직접 실행 — 요청 유실 방지
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}