package com.example.cowmjucraft.global.security;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * 비회원 조회용 비밀번호(주문·지원서)의 최소 강도 정책.
 *
 * <p>요청 제한만으로는 `1234` 같은 비밀번호를 막지 못한다.
 * 도메인별 예외 타입이 달라 검증 결과만 돌려주고 예외는 호출부가 던진다.
 *
 * <p>신규 생성분에만 적용된다. 기존에 저장된 비밀번호의 조회는 영향받지 않는다.
 */
@Component
public class PasswordPolicy {

    private static final int MIN_LENGTH = 8;

    /** bcrypt는 72바이트를 넘는 입력을 잘라내므로 그 이상은 받지 않는다. */
    private static final int MAX_BYTES = 72;

    public boolean isValid(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            return false;
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return false;
        }
        return containsLetter(rawPassword) && containsDigit(rawPassword);
    }

    private boolean containsLetter(String value) {
        return value.chars().anyMatch(Character::isLetter);
    }

    private boolean containsDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }
}
