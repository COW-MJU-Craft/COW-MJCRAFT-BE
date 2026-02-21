package com.example.cowmjucraft.domain.feedback.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class FeedbackException extends DomainException {

    public FeedbackException(FeedbackErrorType errorType) {
        super(errorType);
    }

    public FeedbackException(FeedbackErrorType errorType, String message) {
        super(errorType, message);
    }
}
