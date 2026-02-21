package com.example.cowmjucraft.domain.order.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class OrderException extends DomainException {

    public OrderException(OrderErrorType errorType) {
        super(errorType);
    }

    public OrderException(OrderErrorType errorType, String message) {
        super(errorType, message);
    }
}
