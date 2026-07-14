package com.example.cowmjucraft.domain.order.entity;

public enum MailOutboxEventType {
    ORDER_VIEW_LINK,
    PAID_CONFIRMED,
    CANCELED,
    REFUND_REQUESTED,
    REFUNDED
}
