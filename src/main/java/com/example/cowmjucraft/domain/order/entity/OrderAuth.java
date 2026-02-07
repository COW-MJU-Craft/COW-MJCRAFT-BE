package com.example.cowmjucraft.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "order_auth")
public class OrderAuth {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "lookup_id", nullable = false, unique = true, length = 50)
    private String lookupId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderAuth(Order order, String lookupId, String passwordHash) {
        this.order = order;
        this.lookupId = lookupId;
        this.passwordHash = passwordHash;
    }
}
