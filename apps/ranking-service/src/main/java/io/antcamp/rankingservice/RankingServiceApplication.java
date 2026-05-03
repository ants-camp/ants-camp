package io.antcamp.rankingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RankingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RankingServiceApplication.class, args);
    }
}
