package com.example.cowmjucraft.domain.order.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_policy")
public class OrderPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "default_shipping_fee", nullable = false)
    private int defaultShippingFee;

    @Version
    private Integer version;

    public OrderPolicy(int defaultShippingFee) {
        this.defaultShippingFee = defaultShippingFee;
    }

    public void update(int defaultShippingFee) {
        this.defaultShippingFee = defaultShippingFee;
    }
}
