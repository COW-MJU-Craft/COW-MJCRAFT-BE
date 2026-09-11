package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailServiceTest {

    @ParameterizedTest
    @CsvSource({
            "hong@example.com, h***@example.com",
            "a@example.com, a***@example.com",
            "yunjinkim25@mju.ac.kr, y***@mju.ac.kr",
            "'  hong@example.com  ', h***@example.com",
            "first.last+tag@sub.example.co.kr, f***@sub.example.co.kr"
    })
    void maskEmail_정상주소_로컬파트첫글자만남는다(String email, String expected) {
        // given & when
        String masked = EmailService.maskEmail(email);

        // then
        assertThat(masked).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void maskEmail_값없음_없음표시를반환한다(String email) {
        // given & when
        String masked = EmailService.maskEmail(email);

        // then
        assertThat(masked).isEqualTo("(없음)");
    }

    @ParameterizedTest
    @ValueSource(strings = {"평문문자열", "@example.com", "hong[at]example.com"})
    void maskEmail_주소형식아님_전체를가린다(String email) {
        // given & when
        String masked = EmailService.maskEmail(email);

        // then
        assertThat(masked).isEqualTo("***");
    }

    @Test
    void maskEmail_로컬파트가여러골뱅이포함_마지막골뱅이기준으로자른다() {
        // given
        String email = "a@b@example.com";

        // when
        String masked = EmailService.maskEmail(email);

        // then
        assertThat(masked).isEqualTo("a***@example.com");
    }
}
