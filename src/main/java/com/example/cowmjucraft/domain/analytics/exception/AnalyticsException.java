package com.example.cowmjucraft.domain.analytics.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class AnalyticsException extends DomainException {
    public AnalyticsException(AnalyticsErrorType errorType) {
        super(errorType);
    }

    public AnalyticsException(AnalyticsErrorType errorType, String message) {
        super(errorType, message);
    }
}
