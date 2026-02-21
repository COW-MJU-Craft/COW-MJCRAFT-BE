package com.example.cowmjucraft.domain.introduce.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IntroduceErrorType implements ErrorCode {

    INTRODUCE_NOT_FOUND(404, "소개 정보를 찾을 수 없습니다."),

    PRESIGN_FAILED(500, "업로드 URL 생성에 실패했습니다.");

    private final int httpStatusCode;
    private final String message;
}
