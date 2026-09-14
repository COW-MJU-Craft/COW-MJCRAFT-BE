package com.example.cowmjucraft.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.customer.repository.CustomerRepository;
import com.example.cowmjucraft.global.security.CredentialMatcher;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerCredentialServiceTest {

    private static final String EMAIL = "yunjin@mju.ac.kr";
    private static final String RAW_PASSWORD = "Pa55word!";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerFailureRecorder customerFailureRecorder;

    private PasswordEncoder passwordEncoder;
    private CustomerCredentialService customerCredentialService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        customerCredentialService = new CustomerCredentialService(
                customerRepository,
                new CredentialMatcher(passwordEncoder),
                customerFailureRecorder
        );
    }

    @Test
    void authenticate_비밀번호일치_고객반환() {
        // given
        Customer customer = customerWithPassword(RAW_PASSWORD);
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(customer));

        // when
        Customer result = customerCredentialService.authenticate(EMAIL, RAW_PASSWORD);

        // then
        assertThat(result).isSameAs(customer);
        assertThat(result.getPwFailedAttempts()).isZero();
    }

    @Test
    void authenticate_이메일대소문자달라도_같은고객으로조회() {
        // given
        Customer customer = customerWithPassword(RAW_PASSWORD);
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(customer));

        // when
        Customer result = customerCredentialService.authenticate("  YunJin@MJU.ac.KR  ", RAW_PASSWORD);

        // then
        assertThat(result).isSameAs(customer);
    }

    @Test
    void authenticate_고객없음_INVALID_CREDENTIALS() {
        // given
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerCredentialService.authenticate(EMAIL, RAW_PASSWORD))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.INVALID_CREDENTIALS);
    }

    @Test
    void authenticate_비밀번호미설정_고객없음과동일한예외() {
        // given — 주문만 하고 정보를 저장하지 않은 고객
        Customer customer = new Customer(EMAIL);
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(customer));

        // when & then — 사유가 드러나면 계정 존재 여부를 알아낼 수 있다
        assertThatThrownBy(() -> customerCredentialService.authenticate(EMAIL, RAW_PASSWORD))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.INVALID_CREDENTIALS);
    }

    @Test
    void authenticate_비밀번호불일치_실패를별도트랜잭션에기록한다() {
        // given — 예외가 호출부 트랜잭션을 롤백시키므로 같은 트랜잭션에 기록하면 증가분이 사라진다
        Customer customer = customerWithPassword(RAW_PASSWORD);
        ReflectionTestUtils.setField(customer, "id", 7L);
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(customer));

        // when
        assertThatThrownBy(() -> customerCredentialService.authenticate(EMAIL, "wrong-password-1"))
                .isInstanceOf(CustomerException.class);

        // then
        then(customerFailureRecorder).should().recordPasswordFailure(eq(7L), any());
    }

    @Test
    void recordPasswordFailure_10회누적되면_비밀번호경로가잠긴다() {
        // given
        Customer customer = customerWithPassword(RAW_PASSWORD);

        // when
        for (int attempt = 0; attempt < Customer.MAX_PASSWORD_FAILURES; attempt++) {
            customer.recordPasswordFailure(LocalDateTime.now());
        }

        // then
        assertThat(customer.isPasswordLocked(LocalDateTime.now())).isTrue();
    }

    @Test
    void authenticate_잠금중이면_올바른비밀번호도거부() {
        // given
        Customer customer = customerWithPassword(RAW_PASSWORD);
        ReflectionTestUtils.setField(customer, "pwLockedUntil", LocalDateTime.now().plusMinutes(30));
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(customer));

        // when & then
        assertThatThrownBy(() -> customerCredentialService.authenticate(EMAIL, RAW_PASSWORD))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.INVALID_CREDENTIALS);
    }

    @Test
    void authenticate_잠금중실패는_기록하지않는다() {
        // given — 잠긴 계정을 계속 두드려도 잠금 시간이 연장되면 안 된다
        Customer customer = customerWithPassword(RAW_PASSWORD);
        ReflectionTestUtils.setField(customer, "id", 7L);
        ReflectionTestUtils.setField(customer, "pwFailedAttempts", Customer.MAX_PASSWORD_FAILURES);
        ReflectionTestUtils.setField(customer, "pwLockedUntil", LocalDateTime.now().plusMinutes(30));
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(customer));

        // when
        assertThatThrownBy(() -> customerCredentialService.authenticate(EMAIL, "wrong-password"))
                .isInstanceOf(CustomerException.class);

        // then
        then(customerFailureRecorder).should(never()).recordPasswordFailure(any(), any());
    }

    @Test
    void authenticate_성공시_이전실패횟수초기화() {
        // given
        Customer customer = customerWithPassword(RAW_PASSWORD);
        ReflectionTestUtils.setField(customer, "pwFailedAttempts", 3);
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(customer));

        // when
        customerCredentialService.authenticate(EMAIL, RAW_PASSWORD);

        // then
        assertThat(customer.getPwFailedAttempts()).isZero();
    }

    private Customer customerWithPassword(String rawPassword) {
        Customer customer = new Customer(EMAIL);
        customer.setPassword(passwordEncoder.encode(rawPassword), LocalDateTime.now());
        return customer;
    }
}
