package io.antcamp.tradeservice;

import io.antcamp.tradeservice.infrastructure.client.KisWebSocketClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")   // application-test.yml 를 읽음
@Import(EmbeddedRedisConfig.class)
class TradeServiceApplicationTests {

    KisWebSocketClient kisWebSocketClient;

    @Test
    void contextLoads() {
    }

}
