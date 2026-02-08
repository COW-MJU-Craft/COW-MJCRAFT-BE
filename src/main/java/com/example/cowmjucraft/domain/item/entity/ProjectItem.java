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

    @Column(length = 200)
    private String summary;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSaleType saleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Column(length = 255)
    private String thumbnailKey;

    @Column(length = 255)
    private String journalFileKey;

    @Column(name = "target_qty")
    private Integer targetQty;

    @Column(name = "funded_qty")
    private Integer fundedQty;

    @Column(name = "stock_qty")
    private Integer stockQty;

    public ProjectItem(
            Project project,
            String name,
            String summary,
            String description,
            int price,
            ItemSaleType saleType,
            ItemStatus status,
            ItemType itemType,
            String thumbnailKey,
            String journalFileKey,
            Integer targetQty,
            Integer fundedQty,
            Integer stockQty
    ) {
        this.project = project;
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.price = price;
        this.saleType = saleType;
        this.status = status;
        this.itemType = itemType == null ? ItemType.PHYSICAL : itemType;
        this.thumbnailKey = thumbnailKey;
        this.journalFileKey = journalFileKey;
        this.targetQty = targetQty;
        this.fundedQty = fundedQty;
        this.stockQty = stockQty;
    }

    public ProjectItem(
            Project project,
            String name,
            String summary,
            String description,
            int price,
            ItemSaleType saleType,
            ItemStatus status,
            ItemType itemType,
            String thumbnailKey,
            String journalFileKey,
            Integer targetQty,
            Integer fundedQty
    ) {
        this(
                project,
                name,
                summary,
                description,
                price,
                saleType,
                status,
                itemType,
                thumbnailKey,
                journalFileKey,
                targetQty,
                fundedQty,
                null
        );
    }

    private void updateInternal(
            String name,
            String summary,
            String description,
            int price,
            ItemSaleType saleType,
            ItemStatus status,
            ItemType itemType,
            String thumbnailKey,
            String journalFileKey,
            Integer targetQty,
            Integer fundedQty,
            Integer stockQty
    ) {
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.price = price;
        this.saleType = saleType;
        this.status = status;
        if (itemType != null) {
            this.itemType = itemType;
        }
        this.thumbnailKey = thumbnailKey;
        this.journalFileKey = journalFileKey;
        this.targetQty = targetQty;
        this.fundedQty = fundedQty;
        this.stockQty = stockQty;
    }

    public void update(
            String name,
            String summary,
            String description,
            int price,
            ItemSaleType saleType,
            ItemStatus status,
            ItemType itemType,
            String thumbnailKey,
            String journalFileKey,
            Integer targetQty,
            Integer fundedQty,
            Integer stockQty
    ) {
        updateInternal(
                name,
                summary,
                description,
                price,
                saleType,
                status,
                itemType,
                thumbnailKey,
                journalFileKey,
                targetQty,
                fundedQty,
                stockQty
        );
    }

    public void update(
            String name,
            String summary,
            String description,
            int price,
            ItemSaleType saleType,
            ItemStatus status,
            ItemType itemType,
            String thumbnailKey,
            String journalFileKey,
            Integer targetQty,
            Integer fundedQty
    ) {
        updateInternal(
                name,
                summary,
                description,
                price,
                saleType,
                status,
                itemType,
                thumbnailKey,
                journalFileKey,
                targetQty,
                fundedQty,
                this.stockQty
        );
    }

    public void updateStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public void clearThumbnail() {
        this.thumbnailKey = null;
    }

    public void clearJournalFileKey() {
        this.journalFileKey = null;
    }
}
