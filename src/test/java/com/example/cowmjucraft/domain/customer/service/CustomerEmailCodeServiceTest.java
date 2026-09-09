package com.example.cowmjucraft.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import com.example.cowmjucraft.domain.customer.entity.CustomerEmailCode;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.customer.repository.CustomerEmailCodeRepository;
import com.example.cowmjucraft.global.config.AppProperties;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerEmailCodeServiceTest {

    private static final String EMAIL = "yunjin@mju.ac.kr";

    @Mock
    private CustomerEmailCodeRepository customerEmailCodeRepository;

    @Mock
    private CustomerCodeMailSender customerCodeMailSender;

    @Mock
    private CustomerFailureRecorder customerFailureRecorder;

    private AppProperties appProperties;
    private CustomerEmailCodeService customerEmailCodeService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.setCustomerCodePepper("test-pepper");
        appProperties.setCustomerCodeTtlMinutes(10);
        appProperties.setCustomerCodeResendIntervalSeconds(60);
        appProperties.setCustomerCodeDailyLimit(10);
        customerEmailCodeService = new CustomerEmailCodeService(
                customerEmailCodeRepository,
                customerCodeMailSender,
                customerFailureRecorder,
                appProperties
        );
    }

    @Test
    void issue_정상_코드저장후메일발송() {
        // given
        given(customerEmailCodeRepository.countByEmailAndCreatedAtAfter(eq(EMAIL), any())).willReturn(0);

        // when
        customerEmailCodeService.issue("  YunJin@MJU.ac.KR  ", "127.0.0.1");

        // then — 이메일은 정규화되어 저장된다
        ArgumentCaptor<CustomerEmailCode> captor = ArgumentCaptor.forClass(CustomerEmailCode.class);
        then(customerEmailCodeRepository).should().save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(captor.getValue().getCodeHash()).hasSize(64);
        assertThat(captor.getValue().getRequestIp()).isEqualTo("127.0.0.1");

        // 평문 6자리가 메일로만 나간다
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        then(customerCodeMailSender).should().send(eq(EMAIL), codeCaptor.capture());
        assertThat(codeCaptor.getValue()).hasSize(6).containsOnlyDigits();
    }

    @Test
    void issue_이전코드무효화후_새코드발급() {
        // given
        given(customerEmailCodeRepository.countByEmailAndCreatedAtAfter(eq(EMAIL), any())).willReturn(0);

        // when
        customerEmailCodeService.issue(EMAIL, null);

        // then — 항상 최신 1개만 유효해야 한다
        then(customerEmailCodeRepository).should().consumeAllByEmail(eq(EMAIL), any());
        then(customerEmailCodeRepository).should().save(any());
    }

    @Test
    void issue_60초내재요청_발송하지않음() {
        // given — 재발급 간격 확인이 1건을 반환
        given(customerEmailCodeRepository.countByEmailAndCreatedAtAfter(eq(EMAIL), any())).willReturn(1);

        // when
        customerEmailCodeService.issue(EMAIL, null);

        // then — 조용히 건너뛴다. 호출부는 어차피 같은 202를 돌려준다
        then(customerEmailCodeRepository).should(never()).save(any());
        then(customerCodeMailSender).should(never()).send(anyString(), anyString());
    }

    @Test
    void issue_메일발송실패해도_예외를올리지않는다() {
        // given — 예외가 올라가면 응답이 달라져 "이 이메일은 발송이 시도됐다"는 신호가 된다
        given(customerEmailCodeRepository.countByEmailAndCreatedAtAfter(eq(EMAIL), any())).willReturn(0);
        willThrow(new IllegalStateException("smtp down"))
                .given(customerCodeMailSender).send(anyString(), anyString());

        // when & then
        customerEmailCodeService.issue(EMAIL, null);
        then(customerEmailCodeRepository).should().save(any());
    }

    @Test
    void issue_pepper미설정_CODE_HASH_FAILED() {
        // given
        appProperties.setCustomerCodePepper("  ");
        given(customerEmailCodeRepository.countByEmailAndCreatedAtAfter(eq(EMAIL), any())).willReturn(0);

        // when & then
        assertThatThrownBy(() -> customerEmailCodeService.issue(EMAIL, null))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.CODE_HASH_FAILED);
    }

    @Test
    void verifyAndConsume_코드일치_소진처리() {
        // given
        String code = "481902";
        CustomerEmailCode stored = storedCode(code, LocalDateTime.now().plusMinutes(5));
        given(customerEmailCodeRepository.findFirstByEmailOrderByIdDesc(EMAIL))
                .willReturn(Optional.of(stored));

        // when
        customerEmailCodeService.verifyAndConsume(EMAIL, code, LocalDateTime.now());

        // then
        assertThat(stored.getConsumedAt()).isNotNull();
    }

    @Test
    void verifyAndConsume_코드불일치_실패횟수누적후예외() {
        // given
        CustomerEmailCode stored = storedCode("481902", LocalDateTime.now().plusMinutes(5));
        given(customerEmailCodeRepository.findFirstByEmailOrderByIdDesc(EMAIL))
                .willReturn(Optional.of(stored));

        // when & then
        assertThatThrownBy(() -> customerEmailCodeService.verifyAndConsume(EMAIL, "000000", LocalDateTime.now()))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.INVALID_EMAIL_CODE);
        then(customerFailureRecorder).should().recordCodeFailure(any(), any());
        assertThat(stored.getConsumedAt()).isNull();
    }

    @Test
    void verifyAndConsume_실패횟수가한도에닿은코드는_더이상사용되지않는다() {
        // given
        CustomerEmailCode stored = storedCode("481902", LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(stored, "failedAttempts", CustomerEmailCode.MAX_FAILED_ATTEMPTS);
        given(customerEmailCodeRepository.findFirstByEmailOrderByIdDesc(EMAIL))
                .willReturn(Optional.of(stored));

        // when & then — 올바른 코드를 넣어도 통과하지 못한다
        assertThatThrownBy(() -> customerEmailCodeService.verifyAndConsume(EMAIL, "481902", LocalDateTime.now()))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.INVALID_EMAIL_CODE);
    }

    @Test
    void recordFailure_한도도달시_코드가스스로소진된다() {
        // given
        CustomerEmailCode stored = storedCode("481902", LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(stored, "failedAttempts", CustomerEmailCode.MAX_FAILED_ATTEMPTS - 1);

        // when
        stored.recordFailure(LocalDateTime.now());

        // then
        assertThat(stored.getConsumedAt()).isNotNull();
    }

    @Test
    void verifyAndConsume_만료된코드_INVALID_EMAIL_CODE() {
        // given
        CustomerEmailCode stored = storedCode("481902", LocalDateTime.now().minusMinutes(1));
        given(customerEmailCodeRepository.findFirstByEmailOrderByIdDesc(EMAIL))
                .willReturn(Optional.of(stored));

        // when & then — 만료·불일치·소진을 구분하지 않는다
        assertThatThrownBy(() -> customerEmailCodeService.verifyAndConsume(EMAIL, "481902", LocalDateTime.now()))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.INVALID_EMAIL_CODE);
    }

    @Test
    void verifyAndConsume_코드없음_INVALID_EMAIL_CODE() {
        // given
        given(customerEmailCodeRepository.findFirstByEmailOrderByIdDesc(EMAIL))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerEmailCodeService.verifyAndConsume(EMAIL, "481902", LocalDateTime.now()))
                .isInstanceOf(CustomerException.class);
    }

    @Test
    void verifyAndConsume_코드누락_INVALID_EMAIL_CODE() {
        // when & then
        assertThatThrownBy(() -> customerEmailCodeService.verifyAndConsume(EMAIL, "  ", LocalDateTime.now()))
                .isInstanceOf(CustomerException.class)
                .extracting(exception -> ((CustomerException) exception).getErrorCode())
                .isEqualTo(CustomerErrorType.INVALID_EMAIL_CODE);
    }

    /**
     * 저장된 코드는 평문이 아니라 HMAC 해시다. 서비스와 같은 pepper로 계산해야
     * 검증이 통과하므로 서비스의 해싱 함수를 그대로 호출한다.
     */
    private CustomerEmailCode storedCode(String code, LocalDateTime expiresAt) {
        String hash = (String) ReflectionTestUtils.invokeMethod(customerEmailCodeService, "hash", code);
        return new CustomerEmailCode(EMAIL, hash, expiresAt, null);
    }

    @Test
    void purgeExpired_리포지토리에위임() {
        // given
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        given(customerEmailCodeRepository.deleteAllByExpiresAtBefore(threshold)).willReturn(3);

        // when
        int deleted = customerEmailCodeService.purgeExpired(threshold);

        // then
        assertThat(deleted).isEqualTo(3);
    }
}
