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
@Table(name = "order_fulfillment")
public class OrderFulfillment {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private OrderFulfillmentMethod method;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 50)
    private String receiverPhone;

    @Column(name = "info_confirmed", nullable = false)
    private boolean infoConfirmed;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "delivery_memo", length = 500)
    private String deliveryMemo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderFulfillment(
            Order order,
            OrderFulfillmentMethod method,
            String receiverName,
            String receiverPhone,
            boolean infoConfirmed,
            String postalCode,
            String addressLine1,
            String addressLine2,
            String deliveryMemo
    ) {
        this.order = order;
        this.method = method;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.infoConfirmed = infoConfirmed;
        this.postalCode = postalCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.deliveryMemo = deliveryMemo;
    }
}
