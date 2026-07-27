package com.example.cowmjucraft.global.config.ratelimit;

import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.CommonErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 인증·조회 엔드포인트의 요청 횟수를 제한한다.
 *
 * <p>대부분의 규칙은 <b>실패한 요청만</b> 센다. 공유 IP(캠퍼스 와이파이, 캐리어 NAT)
 * 환경에서 정상 사용자가 서로의 한도를 잠식하지 않게 하기 위해서다.
 * 정상 사용자는 비밀번호를 맞히므로 카운터를 거의 소모하지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRule rule = resolveRule(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // getRemoteAddr()는 server.forward-headers-strategy 설정에 의해
        // X-Forwarded-For의 실제 클라이언트 IP로 해석된다.
        String clientKey = request.getRemoteAddr();

        if (!rateLimitService.hasCapacity(rule, clientKey)) {
            log.warn("요청 제한 초과: rule={}, path={}", rule.getKey(), request.getRequestURI());
            writeTooManyRequests(response, rule);
            return;
        }

        filterChain.doFilter(request, response);

        if (shouldConsume(rule, response.getStatus())) {
            rateLimitService.consume(rule, clientKey);
        }
    }

    private RateLimitRule resolveRule(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(RateLimitRule.values())
                .filter(rule -> rule.matches(path))
                .findFirst()
                .orElse(null);
    }

    /**
     * 실패만 세는 규칙에서는 인증 실패(401)와 대상 없음(404)만 카운트한다.
     * 400·422 같은 입력 형식 오류는 자격증명 추측이 아니므로 세지 않는다.
     */
    private boolean shouldConsume(RateLimitRule rule, int status) {
        if (!rule.isCountOnlyFailures()) {
            return true;
        }
        return status == 401 || status == 404;
    }

    private void writeTooManyRequests(HttpServletResponse response, RateLimitRule rule) throws IOException {
        response.setStatus(CommonErrorType.TOO_MANY_REQUESTS.getHttpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimitService.retryAfterSeconds(rule)));
        objectMapper.writeValue(
                response.getWriter(),
                ApiResult.error(CommonErrorType.TOO_MANY_REQUESTS)
        );
    }
}
