package com.example.cowmjucraft.global.config.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    private static final String CLIENT_IP = "203.0.113.7";

    private RateLimitProperties properties;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setRules(Map.of(
                RateLimitRule.ADMIN_LOGIN.getKey(), limit(3, Duration.ofMinutes(10)),
                RateLimitRule.LOOKUP_ID_AVAILABILITY.getKey(), limit(3, Duration.ofMinutes(1))
        ));
        filter = new RateLimitFilter(properties, new RateLimitService(properties), new ObjectMapper());
    }

    @Test
    void doFilter_로그인성공반복_제한되지않음() throws Exception {
        // given — 실패만 세는 규칙이므로 성공은 카운터를 쓰지 않는다
        FilterChain success = (req, res) -> ((MockHttpServletResponse) res).setStatus(200);

        // when
        for (int i = 0; i < 10; i++) {
            call("/api/admin/login", success);
        }

        // then
        assertThat(call("/api/admin/login", success).getStatus()).isEqualTo(200);
    }

    @Test
    void doFilter_로그인실패반복_한도초과시429() throws Exception {
        // given
        FilterChain unauthorized = (req, res) -> ((MockHttpServletResponse) res).setStatus(401);

        // when — capacity 3 소진
        for (int i = 0; i < 3; i++) {
            assertThat(call("/api/admin/login", unauthorized).getStatus()).isEqualTo(401);
        }

        // then
        MockHttpServletResponse blocked = call("/api/admin/login", unauthorized);
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("600");
        assertThat(blocked.getContentAsString()).contains("요청 횟수가 초과되었습니다.");
    }

    @Test
    void doFilter_입력형식오류는_카운트하지않음() throws Exception {
        // given — 400은 자격증명 추측이 아니다
        FilterChain badRequest = (req, res) -> ((MockHttpServletResponse) res).setStatus(400);

        // when
        for (int i = 0; i < 10; i++) {
            call("/api/admin/login", badRequest);
        }

        // then
        assertThat(call("/api/admin/login", badRequest).getStatus()).isEqualTo(400);
    }

    @Test
    void doFilter_중복확인은_성공응답도카운트() throws Exception {
        // given — 성공 응답 자체가 ID 존재 여부를 노출하므로 전부 센다
        FilterChain success = (req, res) -> ((MockHttpServletResponse) res).setStatus(200);

        // when
        for (int i = 0; i < 3; i++) {
            assertThat(call("/api/orders/lookup-id/availability", success).getStatus()).isEqualTo(200);
        }

        // then
        assertThat(call("/api/orders/lookup-id/availability", success).getStatus()).isEqualTo(429);
    }

    @Test
    void doFilter_제한대상아닌경로_통과() throws Exception {
        // given
        FilterChain unauthorized = (req, res) -> ((MockHttpServletResponse) res).setStatus(401);

        // when — 401을 반복해도 제한 대상이 아니면 통과한다
        for (int i = 0; i < 20; i++) {
            call("/api/projects", unauthorized);
        }

        // then
        assertThat(call("/api/projects", unauthorized).getStatus()).isEqualTo(401);
    }

    @Test
    void doFilter_비활성화되면_제한하지않음() throws Exception {
        // given
        properties.setEnabled(false);
        FilterChain unauthorized = (req, res) -> ((MockHttpServletResponse) res).setStatus(401);

        // when
        for (int i = 0; i < 10; i++) {
            call("/api/admin/login", unauthorized);
        }

        // then
        assertThat(call("/api/admin/login", unauthorized).getStatus()).isEqualTo(401);
    }

    private MockHttpServletResponse call(String path, FilterChain chain) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(CLIENT_IP);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    private RateLimitProperties.Limit limit(int capacity, Duration window) {
        RateLimitProperties.Limit limit = new RateLimitProperties.Limit();
        limit.setCapacity(capacity);
        limit.setWindow(window);
        return limit;
    }
}
