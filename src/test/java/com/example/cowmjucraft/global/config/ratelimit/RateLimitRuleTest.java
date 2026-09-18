package com.example.cowmjucraft.global.config.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimitRuleTest {

    @Test
    void matches_정확히같은경로_참() {
        // given & when & then
        assertThat(RateLimitRule.CUSTOMER_CREDENTIAL.matches("/api/customers/orders")).isTrue();
    }

    @Test
    void matches_하위경로_참() {
        // given — 주문 상세는 /api/customers/orders/{orderId} 형태의 하위 경로다
        // when & then
        assertThat(RateLimitRule.CUSTOMER_CREDENTIAL.matches("/api/customers/orders/42")).isTrue();
    }

    @Test
    void matches_접두사만같고세그먼트가다른경로_거짓() {
        // given — /api/orders/lookup 규칙이 /api/orders/lookup-id/availability를 삼키면 안 된다
        // when & then
        assertThat(RateLimitRule.ORDER_LOOKUP.matches("/api/orders/lookup-id/availability")).isFalse();
    }

    @Test
    void matches_무관한경로_거짓() {
        // given & when & then
        assertThat(RateLimitRule.CUSTOMER_CREDENTIAL.matches("/api/projects")).isFalse();
    }
}
