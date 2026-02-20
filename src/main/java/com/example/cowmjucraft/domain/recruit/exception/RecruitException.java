package com.example.cowmjucraft.domain.recruit.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class RecruitException extends DomainException {

    public RecruitException(RecruitErrorType errorType) {
        super(errorType);
    }

    public RecruitException(RecruitErrorType errorType, String message) {
        super(errorType, message);
    }
}
