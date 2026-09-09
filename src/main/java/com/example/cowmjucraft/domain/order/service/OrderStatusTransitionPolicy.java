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
                || (current == OrderStatus.PAID && requested == OrderStatus.IN_PRODUCTION)
                || (current == OrderStatus.IN_PRODUCTION && requested == OrderStatus.READY_TO_SHIP)
                || (current == OrderStatus.READY_TO_SHIP && requested == OrderStatus.DELIVERED)
                || (current == OrderStatus.PAID && requested == OrderStatus.REFUND_REQUESTED)
                || (current == OrderStatus.REFUND_REQUESTED && requested == OrderStatus.REFUNDED);

        if (!allowed) {
            throw new OrderException(
                    OrderErrorType.INVALID_STATUS_TRANSITION,
                    "current=" + current + ", requested=" + requested
            );
        }
    }

    public static OrderStatus nextNormalStatus(OrderStatus current) {
        return switch (current) {
            case PENDING_DEPOSIT -> OrderStatus.PAID;
            case PAID -> OrderStatus.IN_PRODUCTION;
            case IN_PRODUCTION -> OrderStatus.READY_TO_SHIP;
            case READY_TO_SHIP -> OrderStatus.DELIVERED;
            default -> throw new OrderException(
                    OrderErrorType.INVALID_STATUS_TRANSITION,
                    "current=" + current + ", requested=NEXT_NORMAL_STATUS"
            );
        };
    }
}
