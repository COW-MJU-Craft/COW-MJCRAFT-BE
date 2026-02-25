package com.example.cowmjucraft.domain.payout.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class PayoutException extends DomainException {
    public PayoutException(PayoutErrorType errorType) {
        super(errorType);
    }

    public PayoutException(PayoutErrorType errorType, String message) {
        super(errorType, message);
    }
}
