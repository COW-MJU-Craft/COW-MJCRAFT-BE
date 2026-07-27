package com.example.cowmjucraft.global.config.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RateLimitConfig {

    /**
     * 인증 처리보다 먼저 실행되도록 Security 필터 체인 앞에 등록한다.
     * 제한에 걸린 요청은 비밀번호 검증까지 가지 않는다.
     */
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimitProperties properties,
            RateLimitService rateLimitService,
            ObjectMapper objectMapper
    ) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(
                new RateLimitFilter(properties, rateLimitService, objectMapper)
        );
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
