package com.example.cowmjucraft.global.config.ratelimit;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 요청 제한 대상 엔드포인트.
 *
 * <p>경로와 카운트 방식은 구조적 성격이라 코드에 고정하고,
 * 임계값(capacity/window)만 {@link RateLimitProperties}로 조정한다.
 */
@Getter
@RequiredArgsConstructor
public enum RateLimitRule {

    /** 관리자 로그인 — 실패한 시도만 센다. */
    ADMIN_LOGIN("admin-login", List.of("/api/admin/login"), true),

    /** 관리자 토큰 재발급 — 실패한 시도만 센다. */
    ADMIN_REFRESH("admin-refresh", List.of("/api/admin/refresh"), true),

    /** 비회원 주문 조회 — 실패한 시도만 센다. */
    ORDER_LOOKUP("order-lookup", List.of("/api/orders/lookup"), true),

    /** 지원서·결과 조회 — 실패한 시도만 센다. */
    APPLICATION_READ("application-read", List.of("/api/application/read", "/api/result"), true),

    /**
     * 조회 아이디 중복 확인 — 성공 응답 자체가 "해당 ID 존재" 정보를 노출하므로
     * 성공·실패를 가리지 않고 모든 요청을 센다.
     */
    LOOKUP_ID_AVAILABILITY("lookup-id-availability", List.of("/api/orders/lookup-id/availability"), false);

    private final String key;
    private final List<String> paths;
    private final boolean countOnlyFailures;

    public boolean matches(String requestPath) {
        return paths.contains(requestPath);
    }
}
