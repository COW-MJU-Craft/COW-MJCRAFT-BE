package com.example.cowmjucraft.domain.notice.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminNoticeRequestDtoSizeTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createRequest_imageKeys_20개는_상한위반이_아니다() {
        AdminNoticeCreateRequestDto dto = new AdminNoticeCreateRequestDto("제목", "내용", imageKeys(20));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isFalse();
    }

    @Test
    void createRequest_imageKeys_21개는_상한위반이다() {
        AdminNoticeCreateRequestDto dto = new AdminNoticeCreateRequestDto("제목", "내용", imageKeys(21));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isTrue();
    }

    @Test
    void updateRequest_imageKeys_20개는_상한위반이_아니다() {
        AdminNoticeUpdateRequestDto dto = new AdminNoticeUpdateRequestDto("제목", "내용", imageKeys(20));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isFalse();
    }

    @Test
    void updateRequest_imageKeys_21개는_상한위반이다() {
        AdminNoticeUpdateRequestDto dto = new AdminNoticeUpdateRequestDto("제목", "내용", imageKeys(21));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isTrue();
    }

    private List<String> imageKeys(int count) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add("uploads/notices/images/key-" + i + ".png");
        }
        return keys;
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }
}
