package io.antcamp.notificationservice.infrastructure.config;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("slackActionExecutor")
    public Executor slackActionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("slack-action-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        // Slack 버튼 클릭 → @Async 경계에서 traceId가 끊기지 않도록 context 전파
        executor.setTaskDecorator(runnable -> {
            ContextSnapshot snapshot = ContextSnapshotFactory.builder().build().captureAll();
            return () -> {
                try (ContextSnapshot.Scope ignored = snapshot.setThreadLocals()) {
                    runnable.run();
                }
            };
        });
        executor.initialize();
        return executor;
    }
}