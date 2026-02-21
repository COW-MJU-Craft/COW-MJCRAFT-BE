package com.example.cowmjucraft.domain.project.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class ProjectException extends DomainException {

    public ProjectException(ProjectErrorType errorType) {
        super(errorType);
    }

    public ProjectException(ProjectErrorType errorType, String message) {
        super(errorType, message);
    }
}
