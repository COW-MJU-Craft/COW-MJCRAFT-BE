package com.example.cowmjucraft.domain.customer.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerErrorType implements ErrorCode {

    /**
     * 고객 없음·비밀번호 미설정·잠금 중·불일치를 <b>모두</b> 이 하나로 답한다.
     * 사유를 구분하면 응답만으로 계정 존재 여부를 알아낼 수 있다.
     */
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다. 저장하신 적이 없다면 주문 완료 메일의 조회 링크를 이용해주세요."),

    /** 만료·불일치·소진을 구분하지 않는다. */
    INVALID_EMAIL_CODE(401, "인증 코드가 올바르지 않거나 만료되었습니다."),

    ORDER_NOT_FOUND(404, "주문을 찾을 수 없습니다."),

    REQUIRED_FIELD_MISSING(400, "필수 입력값이 누락되었습니다."),
    INVALID_EMAIL_FORMAT(400, "이메일 형식이 올바르지 않습니다."),

    WEAK_PASSWORD(422, "비밀번호는 8자 이상이며 영문과 숫자를 모두 포함해야 합니다."),

    CODE_HASH_FAILED(500, "인증 코드 처리 중 오류가 발생했습니다.");

    private final int httpStatusCode;
    private final String message;
}
