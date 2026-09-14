package com.example.cowmjucraft.domain.customer.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 소유를 증명하는 1회용 코드.
 *
 * <p>키가 {@code customer_id}가 아니라 {@code email}인 이유: 코드 요청만으로
 * {@link Customer} 행이 생기면 아무나 고객 테이블을 채울 수 있다.
 *
 * <p>코드는 최초 정보 저장(가입)과 비밀번호 재설정 두 곳에서만 쓰인다.
 * 서버가 하는 일이 양쪽 모두 "소유 증명 + 비밀번호 저장"으로 같아서
 * 발급 엔드포인트도 하나다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customer_email_codes")
public class CustomerEmailCode extends BaseTimeEntity {

    /** 코드를 폐기하기까지 허용하는 검증 실패 횟수. */
    public static final int MAX_FAILED_ATTEMPTS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    /** 평문을 저장하지 않는다. HMAC-SHA256(code, 서버 pepper)의 hex. */
    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "request_ip", length = 64)
    private String requestIp;

    public CustomerEmailCode(String email, String codeHash, LocalDateTime expiresAt, String requestIp) {
        this.email = email;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.requestIp = requestIp;
    }

    public boolean isUsable(LocalDateTime now) {
        return consumedAt == null
                && expiresAt.isAfter(now)
                && failedAttempts < MAX_FAILED_ATTEMPTS;
    }

    public void consume(LocalDateTime now) {
        this.consumedAt = now;
    }

    /** 실패가 임계값에 닿으면 코드 자체를 소진 처리해 재시도를 막는다. */
    public void recordFailure(LocalDateTime now) {
        this.failedAttempts++;
        if (this.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            this.consumedAt = now;
        }
    }
}
