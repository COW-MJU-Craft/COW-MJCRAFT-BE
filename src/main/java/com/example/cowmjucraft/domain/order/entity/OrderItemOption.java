package com.example.cowmjucraft.domain.order.entity;

import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_item_options")
public class OrderItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id", nullable = false)
    private ItemOptionValue optionValue;

    @Column(name = "option_group_name_snapshot", nullable = false, length = 100)
    private String optionGroupNameSnapshot;

    @Column(name = "option_value_name_snapshot", nullable = false, length = 100)
    private String optionValueNameSnapshot;

    @Column(name = "additional_price_snapshot", nullable = false)
    private int additionalPriceSnapshot;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderItemOption(
            OrderItem orderItem,
            ItemOptionValue optionValue,
            String optionGroupNameSnapshot,
            String optionValueNameSnapshot,
            int additionalPriceSnapshot
    ) {
        this.orderItem = orderItem;
        this.optionValue = optionValue;
        this.optionGroupNameSnapshot = optionGroupNameSnapshot;
        this.optionValueNameSnapshot = optionValueNameSnapshot;
        this.additionalPriceSnapshot = additionalPriceSnapshot;
    }
}
