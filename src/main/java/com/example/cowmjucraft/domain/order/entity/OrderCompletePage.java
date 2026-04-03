package com.example.cowmjucraft.domain.order.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_complete_pages")
public class OrderCompletePage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_title", nullable = false, length = 200)
    private String messageTitle;

    @Column(name = "message_description", length = 500)
    private String messageDescription;

    @Column(name = "payment_information", nullable = false, length = 1000)
    private String paymentInformation;

    public OrderCompletePage(
            String messageTitle,
            String messageDescription,
            String paymentInformation
    ) {
        this.messageTitle = messageTitle;
        this.messageDescription = messageDescription;
        this.paymentInformation = paymentInformation;
    }

    public void update(
            String messageTitle,
            String messageDescription,
            String paymentInformation
    ) {
        this.messageTitle = messageTitle;
        this.messageDescription = messageDescription;
        this.paymentInformation = paymentInformation;
    }
}