package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;

public final class OrderStatusTransitionPolicy {

    private OrderStatusTransitionPolicy() {
    }

    public static void validate(OrderStatus current, OrderStatus requested) {
        boolean allowed = (current == OrderStatus.PENDING_DEPOSIT && requested == OrderStatus.PAID)
                || (current == OrderStatus.PENDING_DEPOSIT && requested == OrderStatus.CANCELED)
                || (current == OrderStatus.PAID && requested == OrderStatus.REFUND_REQUESTED)
                || (current == OrderStatus.REFUND_REQUESTED && requested == OrderStatus.REFUNDED);

        if (!allowed) {
            throw new OrderException(
                    OrderErrorType.INVALID_STATUS_TRANSITION,
                    "current=" + current + ", requested=" + requested
            );
        }
    }
}
