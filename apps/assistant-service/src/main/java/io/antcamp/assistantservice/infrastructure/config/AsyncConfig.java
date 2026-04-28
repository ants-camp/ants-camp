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
}