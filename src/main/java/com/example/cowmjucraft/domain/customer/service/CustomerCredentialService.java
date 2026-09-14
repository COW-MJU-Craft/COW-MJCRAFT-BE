package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.customer.repository.CustomerRepository;
import com.example.cowmjucraft.global.security.CredentialMatcher;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일 + 비밀번호 검증. 자격증명이 필요한 모든 엔드포인트가 이 하나만 호출한다.
 *
 * <p>세션이 없어 비밀번호가 매 요청에 실리므로 브루트포스 방어가 이 설계의 핵심이다.
 * 방어는 두 겹이다 — 계정 단위 잠금(여기)과 IP 단위 요청 제한
 * ({@code RateLimitRule.CUSTOMER_CREDENTIAL}).
 */
@Service
@RequiredArgsConstructor
public class CustomerCredentialService {

    private final CustomerRepository customerRepository;
    private final CredentialMatcher credentialMatcher;
    private final CustomerFailureRecorder customerFailureRecorder;

    /**
     * 실패 사유 넷 — 고객 없음 · 비밀번호 미설정 · 잠금 중 · 불일치 — 을 모두
     * 같은 {@link CustomerErrorType#INVALID_CREDENTIALS}로 답한다. 사유를 구분하면
     * 응답만으로 계정 존재 여부를 알아낼 수 있다.
     *
     * <p>고객이 없거나 비밀번호가 없어도 {@link CredentialMatcher}가 더미 해시로
     * 동일한 BCrypt 연산을 수행해 응답 시간 차이도 남지 않는다.
     */
    @Transactional
    public Customer authenticate(String rawEmail, String rawPassword) {
        String email = EmailNormalizer.normalize(rawEmail);
        LocalDateTime now = LocalDateTime.now();

        Customer customer = customerRepository.findByEmail(email).orElse(null);

        // 잠금 중이면 해시조차 대조하지 않되, 연산 시간은 동일하게 유지한다.
        boolean locked = customer != null && customer.isPasswordLocked(now);
        String storedHash = (customer == null || locked) ? null : customer.getPasswordHash();

        if (!credentialMatcher.matches(rawPassword, storedHash)) {
            if (customer != null && !locked) {
                // 아래 예외가 이 트랜잭션을 롤백시키므로 증가분은 별도 트랜잭션에 커밋한다.
                customerFailureRecorder.recordPasswordFailure(customer.getId(), now);
            }
            throw new CustomerException(CustomerErrorType.INVALID_CREDENTIALS);
        }

        customer.resetPasswordFailures();
        return customer;
    }
}
