package io.antcamp.tradeservice;

import io.antcamp.tradeservice.infrastructure.client.KisClient;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = KisClient.class)
@EnableDiscoveryClient
@EnableScheduling
@EnableJpaAuditing
public class TradeServiceApplication {

    public static void main(String[] args) {
        // JVM 기본 타임존을 KST(Asia/Seoul)로 고정
        // LocalDateTime.now() 등 모든 시각 연산이 한국 시간 기준으로 동작
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(TradeServiceApplication.class, args);
    }

}
