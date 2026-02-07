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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "stock_deducted_at")
    private LocalDateTime stockDeductedAt;

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
