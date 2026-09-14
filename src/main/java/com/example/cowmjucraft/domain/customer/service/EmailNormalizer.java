package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.exception.CustomerErrorType;
import com.example.cowmjucraft.domain.customer.exception.CustomerException;
import java.util.Locale;

/**
 * 이메일이 고객 식별 키이므로 저장·조회 경로가 <b>모두</b> 이 클래스를 거쳐야 한다.
 * 한 곳이라도 빠지면 {@code Yunjin@…}과 {@code yunjin@…}이 다른 고객이 된다.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(String rawEmail) {
        if (rawEmail == null) {
            throw CustomerException.requiredField(CustomerErrorType.REQUIRED_FIELD_MISSING, "email");
        }
        String trimmed = rawEmail.trim();
        if (trimmed.isEmpty()) {
            throw CustomerException.requiredField(CustomerErrorType.REQUIRED_FIELD_MISSING, "email");
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (normalized.length() > 255 || normalized.indexOf('@') <= 0) {
            throw new CustomerException(CustomerErrorType.INVALID_EMAIL_FORMAT);
        }
        return normalized;
    }

    /** 전화번호는 인증하지 않는 보조 식별자다. 숫자만 남겨 표기 차이를 없앤다. */
    public static String normalizePhone(String rawPhone) {
        if (rawPhone == null) {
            return null;
        }
        String digits = rawPhone.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }
}
