package com.example.cowmjucraft.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordPolicyTest {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @ParameterizedTest
    @ValueSource(strings = {"abcd1234", "Pa55word!", "명지공방2026"})
    void isValid_영문숫자8자이상_true반환(String rawPassword) {
        assertThat(passwordPolicy.isValid(rawPassword)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "1234",          // 너무 짧다
            "abc123",        // 8자 미만
            "abcdefgh",      // 숫자 없음
            "12345678",      // 문자 없음
            "        "       // 공백만
    })
    void isValid_정책미달_false반환(String rawPassword) {
        assertThat(passwordPolicy.isValid(rawPassword)).isFalse();
    }

    @Test
    void isValid_bcrypt한계인72바이트초과_false반환() {
        // given — 한글 1자는 UTF-8에서 3바이트다
        String tooLong = "가".repeat(24) + "a1";

        // when & then
        assertThat(tooLong.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isGreaterThan(72);
        assertThat(passwordPolicy.isValid(tooLong)).isFalse();
    }

    @Test
    void isValid_72바이트경계_true반환() {
        // given — 영문 70자 + 숫자 2자 = 72바이트
        String boundary = "a".repeat(70) + "12";

        // when & then
        assertThat(boundary.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isEqualTo(72);
        assertThat(passwordPolicy.isValid(boundary)).isTrue();
    }
}
