package com.example.cowmjucraft.domain.accounts.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountErrorType implements ErrorCode {

    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_SOCIAL_USER_ID(401, "유효하지 않은 소셜 사용자 ID입니다."),
    OAUTH_PROFILE_FETCH_FAILED(401, "소셜 프로필 조회에 실패했습니다."),
    OAUTH_TOKEN_FETCH_FAILED(401, "소셜 액세스 토큰 발급에 실패했습니다."),

    DUPLICATED_USER_ID(409, "이미 사용 중인 아이디입니다."),

    OAUTH_CLIENT_NOT_CONFIGURED(500, "OAuth 클라이언트가 설정되지 않았습니다.");

    private final int httpStatusCode;
    private final String message;
}
