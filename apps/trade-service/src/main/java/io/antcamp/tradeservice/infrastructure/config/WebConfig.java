package io.antcamp.tradeservice.infrastructure.config;

import io.antcamp.tradeservice.infrastructure.config.resolver.LoginAccountArgumentResolver;
import io.antcamp.tradeservice.infrastructure.config.resolver.LoginUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginUserArgumentResolver loginUserResolver;
    // 수정 전: LoginAccountArgumentResolver 가 빈으로만 존재하고 등록이 누락돼
    // @LoginAccount 파라미터가 항상 null 로 풀리는 버그가 있었음.
    private final LoginAccountArgumentResolver loginAccountResolver;

    public WebConfig(
            LoginUserArgumentResolver loginUserResolver,
            LoginAccountArgumentResolver loginAccountResolver
    ) {
        this.loginUserResolver = loginUserResolver;
        this.loginAccountResolver = loginAccountResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserResolver);
        resolvers.add(loginAccountResolver);
    }
}
