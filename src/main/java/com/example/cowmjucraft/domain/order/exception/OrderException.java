package com.example.cowmjucraft.domain.order.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class OrderException extends DomainException {

    public OrderException(OrderErrorType errorType) {
        super(errorType);
    }

    public OrderException(OrderErrorType errorType, String detail) {
        super(errorType, detail);
    }

    public OrderException(OrderErrorType errorType, String detail, String clientMessage) {
        super(errorType, detail, clientMessage);
    }

    /**
     * 필수 입력값 누락 — 어떤 필드가 비었는지 사용자에게 안내한다.
     */
    public static OrderException requiredField(OrderErrorType errorType, String fieldName) {
        return new OrderException(
                errorType,
                "field=" + fieldName,
                fieldName + "은(는) 필수입니다."
        );
    }
}
