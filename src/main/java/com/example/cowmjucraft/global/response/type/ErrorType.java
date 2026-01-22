package com.example.cowmjucraft.global.response.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    BAD_REQUEST(400, "COMMON_400_BAD_REQUEST", "잘못된 요청입니다."),
    INVALID_REQUEST(400, "COMMON_400_INVALID_REQUEST", "요청 형식이 올바르지 않습니다."),
    UNAUTHORIZED(401, "COMMON_401_UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(403, "COMMON_403_FORBIDDEN", "접근 권한이 없습니다."),
    NOT_FOUND(404, "COMMON_404_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(409, "COMMON_409_CONFLICT", "요청이 현재 상태와 충돌합니다."),
    VALIDATION_FAILED(422, "COMMON_422_VALIDATION_FAILED", "요청 값 검증에 실패하였습니다."),
    INTERNAL_ERROR(500, "COMMON_500_INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");

    private final int httpStatusCode;
    private final String code;
    private final String message;
}