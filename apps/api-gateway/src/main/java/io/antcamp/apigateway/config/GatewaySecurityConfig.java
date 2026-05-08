package io.antcamp.apigateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // CORS preflight 허용
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Prometheus 메트릭 수집 허용
                        .pathMatchers("/actuator/prometheus", "/actuator/health", "/actuator/info").permitAll()

                        // 인증 없이 접근 가능
                        .pathMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/users/register"
                        ).permitAll()

                        // 관리자 전용 API
                        .pathMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // 대회 생성/수정/삭제는 관리자 또는 매니저
                        .pathMatchers(
                                HttpMethod.POST, "/api/competitions/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .pathMatchers(
                                HttpMethod.PUT, "/api/competitions/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .pathMatchers(
                                HttpMethod.DELETE, "/api/competitions/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        // 일반 조회는 로그인 사용자 모두 가능
                        .pathMatchers(HttpMethod.GET, "/api/competitions/**")
                        .hasAnyRole("USER", "MANAGER", "ADMIN")

                        // 계좌/거래 API는 로그인 사용자만 가능
                        .pathMatchers("/api/accounts/**")
                        .hasAnyRole("USER", "MANAGER", "ADMIN")

                        .pathMatchers("/api/trades/**")
                        .hasAnyRole("USER", "MANAGER", "ADMIN")

                        // 나머지는 인증만 필요
                        .anyExchange().authenticated()
                )

                // JWT 검증 활성화
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )

                .build();
    }
    /**
     * JWT의 role claim을 Spring Security 권한으로 변환
     *
     * JWT claim:
     * {
     *   "sub": "userId",
     *   "role": "ROLE_ADMIN"
     * }
     *
     * Spring Security:
     * ROLE_ADMIN 권한으로 인식
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter =
                new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return Flux.empty();
            }

            return Flux.just(new SimpleGrantedAuthority("ROLE_" + role));
        });

        return converter;
    }

    /**
     * JWT 검증 Decoder
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${jwt.secret}") String secret
    ) {
        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .toList()
        );

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
