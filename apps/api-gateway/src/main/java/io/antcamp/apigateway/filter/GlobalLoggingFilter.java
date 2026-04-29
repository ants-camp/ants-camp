package io.antcamp.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "startTime";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        long startTime = System.currentTimeMillis();

        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        log.info("[Gateway Pre] {} {}", method, path);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    Long start = exchange.getAttribute(START_TIME_ATTR);
                    long executionTime = start != null
                            ? System.currentTimeMillis() - start
                            : System.currentTimeMillis() - startTime;

                    var statusCode = exchange.getResponse().getStatusCode();

                    log.info(
                            "[Gateway Post] {} {} status={} time={}ms",
                            method,
                            path,
                            statusCode,
                            executionTime
                    );
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}