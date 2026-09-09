package com.example.cowmjucraft.domain.customer.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class CustomerException extends DomainException {

    public CustomerException(CustomerErrorType errorType) {
        super(errorType);
    }

    public CustomerException(CustomerErrorType errorType, String detail) {
        super(errorType, detail);
    }

    public static CustomerException requiredField(CustomerErrorType errorType, String fieldName) {
        return new CustomerException(errorType, "field=" + fieldName);
    }
}
