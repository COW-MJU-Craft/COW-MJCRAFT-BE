package com.example.cowmjucraft.domain.cart.entity;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import com.example.cowmjucraft.domain.common.BaseTimeEntity;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "item_id"})
        }
)
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ProjectItem item;

    @Column(nullable = false)
    private int quantity;

    public CartItem(Member member, ProjectItem item, int quantity) {
        this.member = member;
        this.item = item;
        this.quantity = quantity;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.quantity += amount;
    }

    public void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.quantity = quantity;
    }
}
