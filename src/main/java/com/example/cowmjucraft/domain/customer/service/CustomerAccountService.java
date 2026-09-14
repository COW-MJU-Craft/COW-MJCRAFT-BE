package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerEnrollRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerProfileRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerProfileSaveRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerEnrollResponseDto;
import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.customer.repository.CustomerRepository;
import com.example.cowmjucraft.global.security.PasswordPolicy;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 계정의 생성·비밀번호 설정·프로필 저장.
 */
@Service
@RequiredArgsConstructor
public class CustomerAccountService {

    private final CustomerRepository customerRepository;
    private final CustomerCreator customerCreator;
    private final CustomerEmailCodeService customerEmailCodeService;
    private final CustomerCredentialService customerCredentialService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;

    /**
     * 주문 생성이 호출한다. 이메일만 있는 빈 고객 행을 만들거나 기존 행을 재사용한다.
     *
     * <p>여기서 프로필과 비밀번호는 <b>절대</b> 건드리지 않는다. 주문 생성은 인증을
     * 받지 않으므로, 남의 이메일로 주문해 그 사람 정보를 덮어쓰는 경로가 되면 안 된다.
     */
    @Transactional
    public Customer upsertForOrder(String rawEmail, LocalDateTime now) {
        String email = EmailNormalizer.normalize(rawEmail);
        Customer customer = customerRepository.findByEmail(email)
                .orElseGet(() -> saveNew(email));
        customer.touchLastOrdered(now);
        return customer;
    }

    /**
     * 정보 등록과 비밀번호 재설정을 겸한다.
     *
     * <p>고객이 없으면 만들고, 있으면 비밀번호를 갱신한다. 그 갱신이 곧 재설정이다.
     * 코드를 받으려면 해당 메일함에 접근할 수 있어야 하므로 정당한 소유자다.
     */
    @Transactional
    public CustomerEnrollResponseDto enroll(CustomerEnrollRequestDto request) {
        String email = EmailNormalizer.normalize(request.email());

        if (!passwordPolicy.isValid(request.password())) {
            throw new CustomerException(CustomerErrorType.WEAK_PASSWORD);
        }

        LocalDateTime now = LocalDateTime.now();
        customerEmailCodeService.verifyAndConsume(email, request.code(), now);

        Customer customer = customerRepository.findByEmail(email)
                .orElseGet(() -> saveNew(email));

        customer.markEmailVerified(now);
        customer.setPassword(passwordEncoder.encode(request.password()), now);

        // profile을 생략하면 기존 프로필을 건드리지 않는다 — 재설정 시나리오.
        if (request.profile() != null) {
            applyProfile(customer, request.profile(), now);
        }

        return new CustomerEnrollResponseDto(customer.getEmail(), customer.hasSavedProfile());
    }

    /** 주문 완료 후 "변경한 정보 저장"과 조회 화면의 "내 정보 수정"이 함께 쓴다. */
    @Transactional
    public CustomerEnrollResponseDto saveProfile(CustomerProfileSaveRequestDto request) {
        Customer customer = customerCredentialService.authenticate(request.email(), request.password());
        applyProfile(customer, request.profile(), LocalDateTime.now());
        return new CustomerEnrollResponseDto(customer.getEmail(), customer.hasSavedProfile());
    }

    private void applyProfile(Customer customer, CustomerProfileRequestDto profile, LocalDateTime now) {
        customer.updateProfile(
                requireText(profile.name(), "name"),
                EmailNormalizer.normalizePhone(profile.phone()),
                profile.buyerType(),
                trimToNull(profile.campus()),
                trimToNull(profile.departmentOrMajor()),
                trimToNull(profile.studentNo()),
                now
        );
    }

    /**
     * 새 고객 행을 만든다. 같은 이메일로 동시에 주문·등록이 들어오면 UNIQUE 제약에 걸리는데,
     * 그 경우 상대가 이미 만든 행을 쓴다.
     *
     * <p>INSERT는 {@link CustomerCreator}가 별도 트랜잭션에서 수행한다 — 실패해도 호출부
     * 트랜잭션을 오염시키지 않아야 재조회 복구가 성립하기 때문이다. INSERT 성공 시 그 엔티티는
     * 별도 트랜잭션 소속이라 detached이므로, 여기서 다시 읽어 <b>현재 영속성 컨텍스트의 managed
     * 엔티티</b>로 돌려준다. 그래야 호출부의 이후 변경(비밀번호 설정·마지막 주문 시각 등)이 flush된다.
     */
    private Customer saveNew(String email) {
        try {
            customerCreator.insert(email);
        } catch (DataIntegrityViolationException exception) {
            // 동시에 같은 이메일로 먼저 만들어졌다 — 아래 재조회가 그 행을 집어온다.
        }
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(CustomerErrorType.CUSTOMER_PERSIST_FAILED));
    }

    private String requireText(String value, String fieldName) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw CustomerException.requiredField(CustomerErrorType.REQUIRED_FIELD_MISSING, fieldName);
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
