package com.example.cowmjucraft.global.response.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessType {

    SUCCESS(200, "요청에 성공하였습니다."),
    CREATED(201, "성공적으로 생성하였습니다."),
    NO_CONTENT(204, "요청에 성공하였습니다."),

    MEDIA_PRESIGN_CREATED(200, "Presign URL 발급 완료"),
    MEDIA_DELETED(200, "미디어 삭제 완료");

    private final int httpStatusCode;
    private final String message;
}
