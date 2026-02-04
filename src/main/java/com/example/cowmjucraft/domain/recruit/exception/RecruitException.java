package com.example.cowmjucraft.domain.recruit.exception;

import com.example.cowmjucraft.global.response.type.ErrorType;
import lombok.Getter;

@Getter
public class RecruitException extends RuntimeException {

    private final ErrorType errorType;

    public RecruitException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public RecruitException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
