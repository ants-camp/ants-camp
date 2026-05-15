package io.antcamp.competitionservice;

import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"io.antcamp.competitionservice", "common"})
@EnableJpaAuditing
@EnableScheduling
@EnableDiscoveryClient
public class CompetitionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompetitionServiceApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system"); // User 서버가 준비되면 제거할 코드
    }
}
