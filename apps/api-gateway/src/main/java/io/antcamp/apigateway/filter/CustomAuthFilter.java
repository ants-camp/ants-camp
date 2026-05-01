package io.antcamp.apigateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.apigateway.dto.User;
import io.antcamp.apigateway.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class CustomAuthFilter extends AbstractGatewayFilterFactory<CustomAuthFilter.Config> {

    /**
     * 인증 없이 접근 가능한 공개 API prefix
     */
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/auth/",
            "/api/public/",
            "/api/users/register"
    );
    private final WebClient webClient;

    public CustomAuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        /**
         * user-service 호출용 WebClient
         * - @LoadBalanced 적용되어 있으므로 서비스명으로 호출 가능
         * - Eureka 기반 서비스 디스커버리 사용
         */
        this.webClient = webClientBuilder
                .baseUrl("http://user-service")
                .build();
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            ServerHttpRequest secureRequest = removeSpoofedHeaders(exchange.getRequest());

            ServerWebExchange secureExchange = exchange.mutate()
                    .request(secureRequest)
                    .build();

            return secureExchange.getPrincipal()
                    .cast(Authentication.class)
                    .flatMap(authentication -> {
                        Jwt jwt = (Jwt) authentication.getPrincipal();

                        String userId = jwt.getSubject();

                        if (userId == null || userId.isBlank()) {
                            return unauthorizedResponse(secureExchange, "JWT subject is empty.");
                        }

                        return authenticateUser(secureExchange, chain, userId);
                    })
                    .switchIfEmpty(unauthorizedResponse(secureExchange, "Authentication is empty."))
                    .onErrorResume(e -> {
                        log.error("[CustomAuthFilter] Authentication error: {}", e.getMessage());
                        return unauthorizedResponse(secureExchange, "Invalid token.");
                    });
        };
    }

    private Mono<Void> authenticateUser(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String userId
    ) {
        return webClient.get()
                .uri("/internal/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .timeout(Duration.ofMillis(500))
                .flatMap(response -> {
                    if (response == null || !response.success() || response.data() == null) {
                        return unauthorizedResponse(exchange, "User authentication failed.");
                    }

                    User user = response.data();

                    if (!"ACTIVE".equals(user.status())) {
                        return unauthorizedResponse(exchange, "User is not active.");
                    }

                    ServerHttpRequest authenticatedRequest =
                            createAuthenticatedRequest(exchange.getRequest(), user);

                    log.info(
                            "[CustomAuthFilter] Authenticated userId={}, email={}, role={}",
                            user.userId(),
                            user.email(),
                            user.role()
                    );

                    return chain.filter(
                            exchange.mutate()
                                    .request(authenticatedRequest)
                                    .build()
                    );
                })
                .onErrorResume(e -> {
                    log.error(
                            "[CustomAuthFilter] User Server error. userId={}, message={}",
                            userId,
                            e.getMessage()
                    );

                    return unauthorizedResponse(
                            exchange,
                            "Authentication server did not respond."
                    );
                });
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private ServerHttpRequest removeSpoofedHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-Role");
                    headers.remove("X-User-Name");
                    headers.remove("X-User-Email");
                    headers.remove("X-User-Phone");
                })
                .build();
    }

    private ServerHttpRequest createAuthenticatedRequest(
            ServerHttpRequest request,
            User user
    ) {
        String encodedName = URLEncoder.encode(
                user.name(),
                StandardCharsets.UTF_8
        );

        return request.mutate()
                .header("X-User-Id", user.userId().toString())
                .header("X-Role", user.role())
                .header("X-User-Name", encodedName)
                .header("X-User-Email", user.email())
                .header("X-User-Phone", user.phone())
                .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                .build();
    }

    private Mono<Void> unauthorizedResponse(
            ServerWebExchange exchange,
            String message
    ) {
        log.warn("[CustomAuthFilter] Authentication failed: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}