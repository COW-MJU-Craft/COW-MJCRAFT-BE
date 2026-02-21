package com.example.cowmjucraft.domain.notice.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class NoticeException extends DomainException {

    public NoticeException(NoticeErrorType errorType) {
        super(errorType);
    }

    public NoticeException(NoticeErrorType errorType, String message) {
        super(errorType, message);
    }
}
