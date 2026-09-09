package com.example.cowmjucraft.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerEnrollRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerProfileRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerProfileSaveRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerEnrollResponseDto;
import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.customer.repository.CustomerRepository;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.global.security.PasswordPolicy;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CustomerAccountServiceTest {

    private static final String EMAIL = "yunjin@mju.ac.kr";
    private static final String PASSWORD = "Pa55word!";
    private static final String CODE = "481902";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerEmailCodeService customerEmailCodeService;

    @Mock
    private CustomerCredentialService customerCredentialService;

    private PasswordEncoder passwordEncoder;
    private CustomerAccountService customerAccountService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        customerAccountService = new CustomerAccountService(
                customerRepository,
                customerEmailCodeService,
                customerCredentialService,
                passwordEncoder,
                new PasswordPolicy()
        );
    }

    @Test
    void enroll_신규고객_생성후비밀번호와프로필저장() {
        // given
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.empty());
        given(customerRepository.saveAndFlush(any(Customer.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CustomerEnrollResponseDto response = customerAccountService.enroll(
                new CustomerEnrollRequestDto(EMAIL, CODE, PASSWORD, profile())
        );

        // then
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        then(customerRepository).should().saveAndFlush(captor.capture());
        Customer saved = captor.getValue();

        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.profileSaved()).isTrue();
        assertThat(saved.getEmailVerifiedAt()).isNotNull();
        assertThat(passwordEncoder.matches(PASSWORD, saved.getPasswordHash())).isTrue();
        assertThat(saved.getName()).isEqualTo("김윤진");
        // 전화번호는 숫자만 남긴다
        assertThat(saved.getPhone()).isEqualTo("01023456789");
    }

    @Test
    void enroll_기존고객_비밀번호갱신되고재설정으로동작() {
        // given — 이미 비밀번호가 있는 고객
        Customer existing = new Customer(EMAIL);
        existing.setPassword(passwordEncoder.encode("OldPass99"), LocalDateTime.now());
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

        // when
        customerAccountService.enroll(new CustomerEnrollRequestDto(EMAIL, CODE, PASSWORD, null));

        // then — 새 행을 만들지 않고 기존 행의 비밀번호만 바꾼다
        then(customerRepository).should(never()).saveAndFlush(any());
        assertThat(passwordEncoder.matches(PASSWORD, existing.getPasswordHash())).isTrue();
    }

    @Test
    void enroll_profile생략시_기존프로필을건드리지않는다() {
        // given
        Customer existing = new Customer(EMAIL);
        existing.updateProfile("기존이름", "01011112222", OrderBuyerType.STAFF, "YONGIN", "학과", "111", LocalDateTime.now());
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

        // when — 비밀번호 재설정 시나리오
        customerAccountService.enroll(new CustomerEnrollRequestDto(EMAIL, CODE, PASSWORD, null));

        // then
        assertThat(existing.getName()).isEqualTo("기존이름");
        assertThat(existing.getBuyerType()).isEqualTo(OrderBuyerType.STAFF);
    }

    @Test
    void enroll_잠긴계정도_코드로재설정하면잠금해제() {
        // given — 비밀번호 10회 실패로 잠긴 계정
        Customer locked = new Customer(EMAIL);
        locked.setPassword(passwordEncoder.encode("OldPass99"), LocalDateTime.now());
        for (int i = 0; i < Customer.MAX_PASSWORD_FAILURES; i++) {
            locked.recordPasswordFailure(LocalDateTime.now());
        }
        assertThat(locked.isPasswordLocked(LocalDateTime.now())).isTrue();
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(locked));

        // when
        customerAccountService.enroll(new CustomerEnrollRequestDto(EMAIL, CODE, PASSWORD, null));

        // then — 코드 경로가 곧 복구 경로다
        assertThat(locked.isPasswordLocked(LocalDateTime.now())).isFalse();
        assertThat(locked.getPwFailedAttempts()).isZero();
    }

    @Test
    void enroll_약한비밀번호_WEAK_PASSWORD이고코드는소모되지않는다() {
        // when & then
        assertThatThrownBy(() -> customerAccountService.enroll(
                new CustomerEnrollRequestDto(EMAIL, CODE, "1234", profile())
        ))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.WEAK_PASSWORD);

        then(customerEmailCodeService).should(never()).verifyAndConsume(anyString(), anyString(), any());
    }

    @Test
    void upsertForOrder_신규이메일_고객생성하고프로필은비운다() {
        // given
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.empty());
        given(customerRepository.saveAndFlush(any(Customer.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime now = LocalDateTime.now();

        // when
        Customer customer = customerAccountService.upsertForOrder("  YunJin@MJU.ac.KR ", now);

        // then — 주문 생성은 인증을 받지 않으므로 비밀번호·프로필을 쓰면 안 된다
        assertThat(customer.getEmail()).isEqualTo(EMAIL);
        assertThat(customer.getPasswordHash()).isNull();
        assertThat(customer.getProfileSavedAt()).isNull();
        assertThat(customer.getLastOrderedAt()).isEqualTo(now);
    }

    @Test
    void upsertForOrder_기존이메일_재사용하고프로필을덮어쓰지않는다() {
        // given — 이미 정보를 저장해둔 고객
        Customer existing = new Customer(EMAIL);
        existing.setPassword(passwordEncoder.encode(PASSWORD), LocalDateTime.now());
        existing.updateProfile("김윤진", "01023456789", OrderBuyerType.STUDENT, "SEOUL", "학부", "60201234", LocalDateTime.now());
        given(customerRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));
        LocalDateTime now = LocalDateTime.now();

        // when — 남이 이 이메일로 주문해도 프로필이 바뀌면 안 된다
        Customer customer = customerAccountService.upsertForOrder(EMAIL, now);

        // then
        assertThat(customer).isSameAs(existing);
        assertThat(customer.getName()).isEqualTo("김윤진");
        assertThat(customer.getPasswordHash()).isNotNull();
        assertThat(customer.getLastOrderedAt()).isEqualTo(now);
        then(customerRepository).should(never()).saveAndFlush(any());
    }

    @Test
    void saveProfile_인증통과시_프로필저장() {
        // given
        Customer customer = new Customer(EMAIL);
        given(customerCredentialService.authenticate(EMAIL, PASSWORD)).willReturn(customer);

        // when
        CustomerEnrollResponseDto response = customerAccountService.saveProfile(
                new CustomerProfileSaveRequestDto(EMAIL, PASSWORD, profile())
        );

        // then
        assertThat(response.profileSaved()).isTrue();
        assertThat(customer.getName()).isEqualTo("김윤진");
        assertThat(customer.getStudentNo()).isEqualTo("60201234");
    }

    private CustomerProfileRequestDto profile() {
        return new CustomerProfileRequestDto(
                "김윤진",
                "010-2345-6789",
                OrderBuyerType.STUDENT,
                "SEOUL",
                "융합소프트웨어학부",
                "60201234"
        );
    }
}
