package com.example.cowmjucraft.domain.order.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "shipping_fee", nullable = false)
    private int shippingFee;

    @Column(name = "final_amount", nullable = false)
    private int finalAmount;

    @Column(name = "deposit_deadline", nullable = false)
    private LocalDateTime depositDeadline;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "stock_deducted_at")
    private LocalDateTime stockDeductedAt;

    @Column(name = "depositor_name", nullable = false, length = 100)
    private String depositorName;

    @Column(name = "privacy_agreed", nullable = false)
    private boolean privacyAgreed;

    @Column(name = "privacy_agreed_at", nullable = false)
    private LocalDateTime privacyAgreedAt;

    @Column(name = "refund_agreed", nullable = false)
    private boolean refundAgreed;

    @Column(name = "refund_agreed_at", nullable = false)
    private LocalDateTime refundAgreedAt;

    @Column(name = "cancel_risk_agreed", nullable = false)
    private boolean cancelRiskAgreed;

    @Column(name = "cancel_risk_agreed_at", nullable = false)
    private LocalDateTime cancelRiskAgreedAt;

    public Order(
            String orderNo,
            OrderStatus status,
            int totalAmount,
            int shippingFee,
            int finalAmount,
            LocalDateTime depositDeadline,
            String depositorName,
            boolean privacyAgreed,
            LocalDateTime privacyAgreedAt,
            boolean refundAgreed,
            LocalDateTime refundAgreedAt,
            boolean cancelRiskAgreed,
            LocalDateTime cancelRiskAgreedAt
    ) {
        this.orderNo = orderNo;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingFee = shippingFee;
        this.finalAmount = finalAmount;
        this.depositDeadline = depositDeadline;
        this.depositorName = depositorName;
        this.privacyAgreed = privacyAgreed;
        this.privacyAgreedAt = privacyAgreedAt;
        this.refundAgreed = refundAgreed;
        this.refundAgreedAt = refundAgreedAt;
        this.cancelRiskAgreed = cancelRiskAgreed;
        this.cancelRiskAgreedAt = cancelRiskAgreedAt;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public void updatePaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void updateStockDeductedAt(LocalDateTime stockDeductedAt) {
        this.stockDeductedAt = stockDeductedAt;
    }
}
