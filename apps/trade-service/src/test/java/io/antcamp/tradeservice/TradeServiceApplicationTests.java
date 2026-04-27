package io.antcamp.tradeservice;

import io.antcamp.tradeservice.config.EmbeddedRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
class TradeServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
