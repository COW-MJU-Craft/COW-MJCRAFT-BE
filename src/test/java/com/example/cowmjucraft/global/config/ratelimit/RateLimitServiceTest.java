package com.example.cowmjucraft.global.config.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    private RateLimitProperties properties;
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setRules(Map.of(
                RateLimitRule.ADMIN_LOGIN.getKey(), limit(3, Duration.ofMinutes(10))
        ));
        rateLimitService = new RateLimitService(properties);
    }

    @Test
    void hasCapacity_한도내소모_true반환() {
        // given
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");

        // when & then
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ADMIN_LOGIN, "1.1.1.1")).isTrue();
    }

    @Test
    void hasCapacity_한도소진_false반환() {
        // given
        for (int i = 0; i < 3; i++) {
            rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        }

        // when & then
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ADMIN_LOGIN, "1.1.1.1")).isFalse();
    }

    @Test
    void hasCapacity_클라이언트키가다르면_서로영향없음() {
        // given
        for (int i = 0; i < 3; i++) {
            rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        }

        // when & then
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ADMIN_LOGIN, "1.1.1.1")).isFalse();
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ADMIN_LOGIN, "2.2.2.2")).isTrue();
    }

    @Test
    void hasCapacity_설정없는규칙_제한하지않음() {
        // given — ORDER_LOOKUP은 rules에 없다
        for (int i = 0; i < 100; i++) {
            rateLimitService.consume(RateLimitRule.ORDER_LOOKUP, "1.1.1.1");
        }

        // when & then
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ORDER_LOOKUP, "1.1.1.1")).isTrue();
    }

    @Test
    void evictIdleBuckets_유휴시간이지나지않은버킷은유지() {
        // given
        properties.setIdleEviction(Duration.ofMinutes(30));
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");

        // when
        rateLimitService.evictIdleBuckets();

        // then — 카운터가 유지되어야 한다
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ADMIN_LOGIN, "1.1.1.1")).isFalse();
    }

    @Test
    void evictIdleBuckets_유휴버킷제거후_카운터초기화() {
        // given
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        rateLimitService.consume(RateLimitRule.ADMIN_LOGIN, "1.1.1.1");
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ADMIN_LOGIN, "1.1.1.1")).isFalse();

        // when — 유휴 기준을 0으로 두어 즉시 제거 대상이 되게 한다
        properties.setIdleEviction(Duration.ZERO);
        rateLimitService.evictIdleBuckets();

        // then
        assertThat(rateLimitService.hasCapacity(RateLimitRule.ADMIN_LOGIN, "1.1.1.1")).isTrue();
    }

    private RateLimitProperties.Limit limit(int capacity, Duration window) {
        RateLimitProperties.Limit limit = new RateLimitProperties.Limit();
        limit.setCapacity(capacity);
        limit.setWindow(window);
        return limit;
    }
}
