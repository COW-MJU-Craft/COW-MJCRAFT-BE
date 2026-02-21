package com.example.cowmjucraft.domain.accounts.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class AccountException extends DomainException {

    public AccountException(AccountErrorType errorType) {
        super(errorType);
    }

    public AccountException(AccountErrorType errorType, String message) {
        super(errorType, message);
    }
}
