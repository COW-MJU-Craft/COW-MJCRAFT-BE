package com.example.cowmjucraft.domain.project.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectErrorType implements ErrorCode {

    PROJECT_NOT_FOUND(404, "프로젝트를 찾을 수 없습니다."),

    ORDER_PATCH_VALIDATION_FAILED(422, "프로젝트 순서 변경 요청 값이 올바르지 않습니다."),
    FILE_REQUIRED(422, "파일 목록이 필요합니다.");

    private final int httpStatusCode;
    private final String message;
}
