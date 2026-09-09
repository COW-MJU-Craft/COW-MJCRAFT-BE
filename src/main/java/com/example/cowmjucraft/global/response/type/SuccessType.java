package com.example.cowmjucraft.global.response.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessType {

    SUCCESS(200, "요청에 성공하였습니다."),
    CREATED(201, "성공적으로 생성하였습니다."),

    /** 접수만 하고 결과를 알리지 않는 응답. 이메일 인증 코드 발송이 쓴다. */
    ACCEPTED(202, "요청을 접수하였습니다."),

    MEDIA_PRESIGN_CREATED(200, "Presign URL 발급 완료"),
    MEDIA_DELETED(200, "미디어 삭제 완료");

    private final int httpStatusCode;
    private final String message;
}