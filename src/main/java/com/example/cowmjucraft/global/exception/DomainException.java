package com.example.cowmjucraft.global.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    protected DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
