package com.example.cowmjucraft.domain.mypage.exception;

import com.example.cowmjucraft.global.exception.DomainException;

public class MyPageException extends DomainException {

    public MyPageException(MyPageErrorType errorType) {
        super(errorType);
    }

    public MyPageException(MyPageErrorType errorType, String message) {
        super(errorType, message);
    }
}
