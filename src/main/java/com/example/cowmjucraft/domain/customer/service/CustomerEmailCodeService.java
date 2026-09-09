package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.entity.CustomerEmailCode;
import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import com.example.cowmjucraft.domain.customer.repository.CustomerEmailCodeRepository;
import com.example.cowmjucraft.global.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일 소유 증명 코드의 발급과 검증.
 *
 * <p>발급 결과는 <b>항상 동일하게</b> 응답해야 한다. 이메일이 등록돼 있든 아니든,
 * 요청 제한에 걸렸든 아니든 호출부는 같은 202를 돌려준다. 사유가 응답에 드러나면
 * 그것만으로 계정 존재 여부를 알아낼 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerEmailCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int CODE_BOUND = 1_000_000;

    private final CustomerEmailCodeRepository customerEmailCodeRepository;
    private final CustomerCodeMailSender customerCodeMailSender;
    private final CustomerFailureRecorder customerFailureRecorder;
    private final AppProperties appProperties;

    /**
     * 코드를 발급하고 메일로 보낸다. 요청 제한에 걸리면 조용히 건너뛴다.
     *
     * <p>메일 발송 실패도 삼킨다. 여기서 예외를 올리면 응답이 달라져
     * "이 이메일은 발송이 시도됐다"는 신호가 된다.
     */
    @Transactional
    public void issue(String rawEmail, String requestIp) {
        String email = EmailNormalizer.normalize(rawEmail);
        LocalDateTime now = LocalDateTime.now();

        if (isThrottled(email, now)) {
            log.warn("인증 코드 발급 제한: email={}", maskEmail(email));
            return;
        }

        // 항상 최신 코드 하나만 유효하도록 이전 코드를 모두 무효화한다.
        customerEmailCodeRepository.consumeAllByEmail(email, now);

        String code = generateCode();
        customerEmailCodeRepository.save(new CustomerEmailCode(
                email,
                hash(code),
                now.plusMinutes(appProperties.getCustomerCodeTtlMinutes()),
                requestIp
        ));

        try {
            customerCodeMailSender.send(email, code);
        } catch (RuntimeException exception) {
            log.error("인증 코드 메일 발송 실패: email={}", maskEmail(email), exception);
        }
    }

    /**
     * 코드를 검증하고 소진 처리한다.
     *
     * @throws CustomerException 코드가 없거나 만료·불일치·소진된 경우.
     *                           사유를 구분하지 않고 같은 예외를 던진다.
     */
    @Transactional
    public void verifyAndConsume(String normalizedEmail, String rawCode, LocalDateTime now) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new CustomerException(CustomerErrorType.INVALID_EMAIL_CODE);
        }

        CustomerEmailCode emailCode = customerEmailCodeRepository
                .findFirstByEmailOrderByIdDesc(normalizedEmail)
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> new CustomerException(CustomerErrorType.INVALID_EMAIL_CODE));

        if (!MessageDigest.isEqual(
                hash(rawCode.trim()).getBytes(StandardCharsets.UTF_8),
                emailCode.getCodeHash().getBytes(StandardCharsets.UTF_8)
        )) {
            // 아래 예외가 이 트랜잭션을 롤백시키므로 증가분은 별도 트랜잭션에 커밋한다.
            customerFailureRecorder.recordCodeFailure(emailCode.getId(), now);
            throw new CustomerException(CustomerErrorType.INVALID_EMAIL_CODE);
        }

        emailCode.consume(now);
    }

    /** 만료된 지 오래된 코드를 지운다. 남겨둘 이유가 없는 1회용 데이터다. */
    @Transactional
    public int purgeExpired(LocalDateTime threshold) {
        return customerEmailCodeRepository.deleteAllByExpiresAtBefore(threshold);
    }

    private boolean isThrottled(String email, LocalDateTime now) {
        LocalDateTime resendThreshold = now.minusSeconds(appProperties.getCustomerCodeResendIntervalSeconds());
        if (customerEmailCodeRepository.countByEmailAndCreatedAtAfter(email, resendThreshold) > 0) {
            return true;
        }
        LocalDateTime dailyThreshold = now.minusDays(1);
        return customerEmailCodeRepository.countByEmailAndCreatedAtAfter(email, dailyThreshold)
                >= appProperties.getCustomerCodeDailyLimit();
    }

    private String generateCode() {
        return String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(CODE_BOUND));
    }

    /**
     * 6자리는 경우의 수가 100만이라 평문 해시는 무지개 테이블로 즉시 역산된다.
     * 서버 pepper를 키로 쓰는 HMAC이라야 DB 유출만으로는 되돌릴 수 없다.
     */
    private String hash(String code) {
        String pepper = appProperties.getCustomerCodePepper();
        if (pepper == null || pepper.isBlank()) {
            throw new CustomerException(CustomerErrorType.CODE_HASH_FAILED, "app.customer-code-pepper 미설정");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(code.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new CustomerException(CustomerErrorType.CODE_HASH_FAILED);
        }
    }

    /** 로그에 이메일 전체를 남기지 않는다. */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        String local = at <= 0 ? email : email.substring(0, at);
        String domain = at <= 0 ? "" : email.substring(at);
        String visible = local.length() <= 2 ? local.substring(0, 1) : local.substring(0, 2);
        return visible + "***" + domain;
    }

    /** 테스트에서 쓰기 위한 조회 헬퍼. */
    @Transactional(readOnly = true)
    public Optional<CustomerEmailCode> findLatest(String normalizedEmail) {
        return customerEmailCodeRepository.findFirstByEmailOrderByIdDesc(normalizedEmail);
    }
}
