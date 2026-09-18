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
@Table(name = "item_option_groups")
public class ItemOptionGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ProjectItem item;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ItemOptionGroup(ProjectItem item, String name, boolean required, int sortOrder) {
        this.item = item;
        this.name = name;
        this.required = required;
        this.sortOrder = sortOrder;
    }

    public void update(String name, boolean required, int sortOrder) {
        this.name = name;
        this.required = required;
        this.sortOrder = sortOrder;
    }
}
