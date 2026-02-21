package com.example.cowmjucraft.domain.sns.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SnsErrorType implements ErrorCode {

    SNS_LINK_NOT_FOUND(404, "SNS 링크를 찾을 수 없습니다.");

    private final int httpStatusCode;
    private final String message;
}
