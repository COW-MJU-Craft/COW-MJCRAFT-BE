package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class OrderStatusTransitionPolicy {

    private OrderStatusTransitionPolicy() {
    }

    public static void validate(OrderStatus current, OrderStatus requested) {
        boolean allowed = (current == OrderStatus.PENDING_DEPOSIT && requested == OrderStatus.PAID)
                || (current == OrderStatus.PENDING_DEPOSIT && requested == OrderStatus.CANCELED)
                || (current == OrderStatus.PAID && requested == OrderStatus.REFUND_REQUESTED)
                || (current == OrderStatus.REFUND_REQUESTED && requested == OrderStatus.REFUNDED);

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "주문 상태 전이가 허용되지 않습니다. current=" + current + ", requested=" + requested
            );
        }
    }
}
