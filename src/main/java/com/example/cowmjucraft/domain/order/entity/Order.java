package com.example.cowmjucraft.domain.order.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import com.example.cowmjucraft.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_orders_project_order_no",
                columnNames = {"representative_project_id", "project_order_no"}
        )
)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_project_id", nullable = false)
    private Project representativeProject;

    @Column(name = "project_order_no", nullable = false)
    private long projectOrderNo;

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

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "refund_requested_at")
    private LocalDateTime refundRequestedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

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
            Project representativeProject,
            long projectOrderNo,
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
        this.representativeProject = representativeProject;
        this.projectOrderNo = projectOrderNo;
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

    public void confirmPaid(LocalDateTime paidAt, LocalDateTime stockDeductedAt) {
        this.status = OrderStatus.PAID;
        this.paidAt = paidAt;
        this.stockDeductedAt = stockDeductedAt;
    }

    public void cancelPendingDeposit(LocalDateTime canceledAt, String reason) {
        this.status = OrderStatus.CANCELED;
        this.canceledAt = canceledAt;
        this.cancelReason = reason;
    }

    public void requestRefund(LocalDateTime refundRequestedAt, String reason) {
        this.status = OrderStatus.REFUND_REQUESTED;
        this.refundRequestedAt = refundRequestedAt;
        this.cancelReason = reason;
    }

    public void confirmRefund(LocalDateTime refundedAt) {
        this.status = OrderStatus.REFUNDED;
        this.refundedAt = refundedAt;
    }
}
