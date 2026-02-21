package com.example.cowmjucraft.domain.item.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class ItemException extends DomainException {

    public ItemException(ItemErrorType errorType) {
        super(errorType);
    }

    public ItemException(ItemErrorType errorType, String message) {
        super(errorType, message);
    }
}
