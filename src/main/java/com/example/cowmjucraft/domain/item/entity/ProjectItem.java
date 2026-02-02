package com.example.cowmjucraft.domain.item.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import com.example.cowmjucraft.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "project_items")
public class ProjectItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(length = 100, nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSaleType saleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(length = 255)
    private String thumbnailKey;

    @Column(name = "target_qty")
    private Integer targetQty;

    @Column(name = "funded_qty", nullable = false)
    private int fundedQty;

    public ProjectItem(
            Project project,
            String name,
            String description,
            int price,
            ItemSaleType saleType,
            ItemStatus status,
            String thumbnailKey,
            Integer targetQty,
            int fundedQty
    ) {
        this.project = project;
        this.name = name;
        this.description = description;
        this.price = price;
        this.saleType = saleType;
        this.status = status;
        this.thumbnailKey = thumbnailKey;
        this.targetQty = targetQty;
        this.fundedQty = fundedQty;
    }

    public void update(
            String name,
            String description,
            int price,
            ItemSaleType saleType,
            ItemStatus status,
            String thumbnailKey,
            Integer targetQty,
            int fundedQty
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.saleType = saleType;
        this.status = status;
        this.thumbnailKey = thumbnailKey;
        this.targetQty = targetQty;
        this.fundedQty = fundedQty;
    }
}
