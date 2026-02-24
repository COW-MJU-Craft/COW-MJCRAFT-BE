package com.example.cowmjucraft.domain.payout.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payout_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payout_id", nullable = false)
    private Payout payout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PayoutItemType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private long amount;

    @Column(length = 50)
    private String category;

    public PayoutItem(PayoutItemType type, String name, long amount, String category) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
    }

    void attach(Payout payout) {
        this.payout = payout;
    }

    void detachPayout() {
        this.payout = null;
    }

    public void update(PayoutItemType type, String name, long amount, String category) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
    }
}