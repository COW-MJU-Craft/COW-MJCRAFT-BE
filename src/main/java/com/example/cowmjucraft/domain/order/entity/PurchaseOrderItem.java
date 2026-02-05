package com.example.cowmjucraft.domain.order.entity;

import com.example.cowmjucraft.domain.item.entity.ProjectItem;
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
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ProjectItem item;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int linePrice;

    public PurchaseOrderItem(PurchaseOrder order, ProjectItem item, int quantity) {
        this.order = order;
        this.item = item;
        this.itemName = item.getName();
        this.unitPrice = item.getPrice();
        this.quantity = quantity;
        this.linePrice = item.getPrice() * quantity;
    }
}
