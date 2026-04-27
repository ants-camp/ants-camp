package io.antcamp.tradeservice;

import io.antcamp.tradeservice.infrastructure.client.KisWebSocketClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
class TradeServiceApplicationTests {

    // Spring Boot 3.4+ 에서 @MockBean 대신 @MockitoBean 사용
    // Spring 컨텍스트 내 실제 빈을 Mock으로 교체 → @PostConstruct connect() 실행 차단
    @MockitoBean
    KisWebSocketClient kisWebSocketClient;

    @Test
    void contextLoads() {
    }

}
