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
    LOOKUP_ID_AVAILABILITY("lookup-id-availability", List.of("/api/orders/lookup-id/availability"), false),

    /**
     * 이메일 인증 코드 발송 — 성공·실패를 가리지 않고 모두 센다.
     * 응답이 항상 202라 실패만 세는 방식으로는 아무것도 막지 못하고,
     * 남의 메일함으로 코드를 쏟아붓는 것 자체를 막아야 한다.
     */
    CUSTOMER_EMAIL_CODE("customer-email-code", List.of("/api/customers/email-code"), false),

    /**
     * 고객 자격증명을 받는 엔드포인트 — 실패한 시도만 센다.
     * 세션이 없어 비밀번호가 매 요청에 실리므로 계정 단위 잠금
     * ({@code Customer.MAX_PASSWORD_FAILURES})과 함께 두 겹으로 막는다.
     */
    CUSTOMER_CREDENTIAL(
            "customer-credential",
            List.of("/api/customers/prefill", "/api/customers/profile", "/api/customers/orders"),
            true
    ),

    /** 코드 검증 — 실패한 시도만 센다. */
    CUSTOMER_ENROLL("customer-enroll", List.of("/api/customers/enroll"), true);

    private final String key;
    private final List<String> paths;
    private final boolean countOnlyFailures;

    public boolean matches(String requestPath) {
        return paths.contains(requestPath);
    }
}
