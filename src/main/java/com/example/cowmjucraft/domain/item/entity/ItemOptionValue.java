package com.example.cowmjucraft.domain.item.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "item_option_values")
public class ItemOptionValue extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false)
    private ItemOptionGroup optionGroup;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "additional_price", nullable = false)
    private int additionalPrice;

    @Column(name = "stock_qty")
    private Integer stockQty;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ItemOptionValue(ItemOptionGroup optionGroup, String name, int additionalPrice, Integer stockQty, int sortOrder) {
        this.optionGroup = optionGroup;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.stockQty = stockQty;
        this.sortOrder = sortOrder;
    }

    public void update(String name, int additionalPrice, Integer stockQty, int sortOrder) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.stockQty = stockQty;
        this.sortOrder = sortOrder;
    }

    public void updateStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }
}
