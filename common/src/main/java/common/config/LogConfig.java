package common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfig {

    private static final Logger log = LoggerFactory.getLogger(LogConfig.class);

    /**
     * 애플리케이션 시작 시 로그 확인
     */
    @PostConstruct
    public void init() {
        log.info("======================================");
        log.info(" Common LogConfig Initialized");
        log.info(" Logging system is ready");
        log.info("======================================");
    }
}