package com.example.cowmjucraft.domain.mypage.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MyPageErrorType implements ErrorCode {

    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    ADDRESS_NOT_FOUND(404, "배송지를 찾을 수 없습니다."),

    ADDRESS_ALREADY_EXISTS(409, "이미 등록된 배송지가 있습니다.");

    private final int httpStatusCode;
    private final String message;
}
