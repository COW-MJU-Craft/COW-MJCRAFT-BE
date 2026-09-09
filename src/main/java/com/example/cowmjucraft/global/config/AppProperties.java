package com.example.cowmjucraft.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String publicBaseUrl;
    private String orderViewPath;
    private long orderViewTokenTtlMinutes;

    /**
     * 메일 링크에 조회 토큰을 싣는 방식.
     *
     * <p>{@code QUERY}는 {@code ?token=...}으로, 액세스 로그·프록시 로그·Referer 헤더에 토큰이 남는다.
     * {@code FRAGMENT}는 {@code #token=...}으로, fragment는 서버로 전송되지 않아 로그에 남지 않는다.
     *
     * <p>기본값은 기존 동작인 {@code QUERY}다. 프런트엔드가 {@code location.hash}에서 토큰을 읽도록
     * 대응을 마치면 {@code FRAGMENT}로 전환한다.
     */
    private OrderViewTokenDelivery orderViewTokenDelivery = OrderViewTokenDelivery.QUERY;

    public enum OrderViewTokenDelivery {
        QUERY,
        FRAGMENT
    }
}
