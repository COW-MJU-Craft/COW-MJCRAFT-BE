package com.example.cowmjucraft.domain.order.entity;

public enum OrderStatus {
    PENDING_DEPOSIT,
    PAID,
    IN_PRODUCTION,
    READY_TO_SHIP,
    DELIVERED,
    CANCELED,
    REFUND_REQUESTED,
    REFUNDED
}
