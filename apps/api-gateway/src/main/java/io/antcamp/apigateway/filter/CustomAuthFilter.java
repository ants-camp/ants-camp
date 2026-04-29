package io.antcamp.apigateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.apigateway.dto.User;
import io.antcamp.apigateway.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
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

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/auth/",
            "/api/public/",
            "/api/users/register"
    );
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public CustomAuthFilter(ObjectMapper objectMapper, WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl("http://user-server")
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

            String authHeader = secureRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse(secureExchange, "Token not found.");
            }

            String token = authHeader.substring(7);
            String userId;

            try {
                userId = extractUserIdFromJwt(token);
            } catch (Exception e) {
                return unauthorizedResponse(secureExchange, "Invalid token format.");
            }

            return webClient.get()
                    .uri("/internal/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .timeout(Duration.ofMillis(500))
                    .switchIfEmpty(Mono.error(new RuntimeException("EMPTY_RESPONSE")))
                    .flatMap(response -> {
                        if (response == null || !response.success() || response.data() == null) {
                            return unauthorizedResponse(secureExchange, "User authentication failed.");
                        }

                        User user = response.data();

                        if (!"ACTIVE".equals(user.status())) {
                            return unauthorizedResponse(secureExchange, "User is not active.");
                        }

                        ServerHttpRequest authenticatedRequest =
                                createAuthenticatedRequest(secureExchange.getRequest(), user);

                        log.info(
                                "[CustomAuthFilter] Authenticated userId={}, email={}, role={}",
                                user.userId(),
                                user.email(),
                                user.role()
                        );

                        return chain.filter(
                                secureExchange.mutate()
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
                                secureExchange,
                                "Authentication server did not respond."
                        );
                    });
        };
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

    private String extractUserIdFromJwt(String token) throws Exception {
        String[] parts = token.split("\\.");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT");
        }

        String payload = new String(
                Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
        );

        JsonNode jsonNode = objectMapper.readTree(payload);
        String userId = jsonNode.path("sub").asText();

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("JWT subject is empty");
        }

        return userId;
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