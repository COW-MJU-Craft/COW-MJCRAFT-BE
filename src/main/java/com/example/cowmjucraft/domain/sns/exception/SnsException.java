package com.example.cowmjucraft.domain.sns.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class SnsException extends DomainException {

    public SnsException(SnsErrorType errorType) {
        super(errorType);
    }

    public SnsException(SnsErrorType errorType, String message) {
        super(errorType, message);
    }
}
