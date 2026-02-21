package com.example.cowmjucraft.domain.introduce.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class IntroduceException extends DomainException {

    public IntroduceException(IntroduceErrorType errorType) {
        super(errorType);
    }

    public IntroduceException(IntroduceErrorType errorType, String message) {
        super(errorType, message);
    }
}
