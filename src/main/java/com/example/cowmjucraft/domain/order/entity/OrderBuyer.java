package com.example.cowmjucraft.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_buyer")
public class OrderBuyer {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_type", nullable = false)
    private OrderBuyerType buyerType;

    @Column(name = "campus", length = 50)
    private String campus;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "department_or_major", length = 100)
    private String departmentOrMajor;

    @Column(name = "student_no", length = 50)
    private String studentNo;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "refund_bank", nullable = false, length = 100)
    private String refundBank;

    @Column(name = "refund_account", nullable = false, length = 100)
    private String refundAccount;

    @Column(name = "referral_source", length = 100)
    private String referralSource;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderBuyer(
            Order order,
            OrderBuyerType buyerType,
            String campus,
            String name,
            String departmentOrMajor,
            String studentNo,
            String phone,
            String refundBank,
            String refundAccount,
            String referralSource,
            String email
    ) {
        this.order = order;
        this.buyerType = buyerType;
        this.campus = campus;
        this.name = name;
        this.departmentOrMajor = departmentOrMajor;
        this.studentNo = studentNo;
        this.phone = phone;
        this.refundBank = refundBank;
        this.refundAccount = refundAccount;
        this.referralSource = referralSource;
        this.email = email;
    }
}
