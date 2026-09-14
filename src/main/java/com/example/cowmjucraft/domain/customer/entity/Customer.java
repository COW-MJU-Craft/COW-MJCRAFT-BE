package com.example.cowmjucraft.domain.customer.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일로 식별되는 고객.
 *
 * <p>로그인 상태(세션)를 두지 않는다. 불러오기·조회·저장은 각각 독립된 일회성
 * 요청이고, 그때마다 이메일 + 비밀번호를 제시받아 검증한다.
 *
 * <p>프로필에 담는 것은 이름·연락처·구분·캠퍼스·학과/학번까지다.
 * 환불 은행/계좌·입금자명·수령지는 <b>컬럼조차 두지 않는다</b> — 비밀번호 하나가
 * 계좌번호로 직결되지 않게 하기 위해서다. 불러오기는 그 값들을 최근 주문
 * 스냅샷({@code order_buyer}/{@code order_fulfillment})에서 읽어 채운다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customers")
public class Customer extends BaseTimeEntity {

    /** 비밀번호 로그인을 잠그기까지 허용하는 연속 실패 횟수. */
    public static final int MAX_PASSWORD_FAILURES = 10;

    /** 잠금 지속 시간(분). 코드 경로는 이 잠금의 영향을 받지 않는다. */
    public static final int PASSWORD_LOCK_MINUTES = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 정규화(trim + 소문자)된 값만 저장한다. */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    /** null이면 아직 정보를 저장하지 않은 고객이다. 주문만으로는 채워지지 않는다. */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "password_set_at")
    private LocalDateTime passwordSetAt;

    @Column(name = "pw_failed_attempts", nullable = false)
    private int pwFailedAttempts;

    @Column(name = "pw_locked_until")
    private LocalDateTime pwLockedUntil;

    /** 인증하지 않는 보조 식별자. 숫자만 남겨 저장한다. */
    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_type", length = 20)
    private OrderBuyerType buyerType;

    @Column(length = 50)
    private String campus;

    @Column(name = "department_or_major", length = 100)
    private String departmentOrMajor;

    @Column(name = "student_no", length = 50)
    private String studentNo;

    /** null이면 사용자가 직접 저장한 프로필이 없다는 뜻이다. */
    @Column(name = "profile_saved_at")
    private LocalDateTime profileSavedAt;

    @Column(name = "last_ordered_at")
    private LocalDateTime lastOrderedAt;

    public Customer(String email) {
        this.email = email;
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }

    public boolean hasSavedProfile() {
        return profileSavedAt != null;
    }

    public boolean isPasswordLocked(LocalDateTime now) {
        return pwLockedUntil != null && pwLockedUntil.isAfter(now);
    }

    public void markEmailVerified(LocalDateTime now) {
        if (this.emailVerifiedAt == null) {
            this.emailVerifiedAt = now;
        }
    }

    public void setPassword(String passwordHash, LocalDateTime now) {
        this.passwordHash = passwordHash;
        this.passwordSetAt = now;
        resetPasswordFailures();
    }

    /**
     * 연속 실패를 누적하고 임계값에 닿으면 비밀번호 경로만 잠근다.
     * 코드로 재설정하는 경로는 잠기지 않아야 복구가 가능하다.
     */
    public void recordPasswordFailure(LocalDateTime now) {
        this.pwFailedAttempts++;
        if (this.pwFailedAttempts >= MAX_PASSWORD_FAILURES) {
            this.pwLockedUntil = now.plusMinutes(PASSWORD_LOCK_MINUTES);
        }
    }

    public void resetPasswordFailures() {
        this.pwFailedAttempts = 0;
        this.pwLockedUntil = null;
    }

    public void updateProfile(
            String name,
            String phone,
            OrderBuyerType buyerType,
            String campus,
            String departmentOrMajor,
            String studentNo,
            LocalDateTime now
    ) {
        this.name = name;
        this.phone = phone;
        this.buyerType = buyerType;
        this.campus = campus;
        this.departmentOrMajor = departmentOrMajor;
        this.studentNo = studentNo;
        this.profileSavedAt = now;
    }

    public void touchLastOrdered(LocalDateTime now) {
        this.lastOrderedAt = now;
    }
}
