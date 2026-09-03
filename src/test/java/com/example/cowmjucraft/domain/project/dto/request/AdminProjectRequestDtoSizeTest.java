package com.example.cowmjucraft.domain.project.dto.request;

import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminProjectRequestDtoSizeTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createRequest_imageKeys_20개는_상한위반이_아니다() {
        AdminProjectCreateRequestDto dto = createRequest(imageKeys(20));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isFalse();
    }

    @Test
    void createRequest_imageKeys_21개는_상한위반이다() {
        AdminProjectCreateRequestDto dto = createRequest(imageKeys(21));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isTrue();
    }

    @Test
    void updateRequest_imageKeys_20개는_상한위반이_아니다() {
        AdminProjectUpdateRequestDto dto = updateRequest(imageKeys(20));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isFalse();
    }

    @Test
    void updateRequest_imageKeys_21개는_상한위반이다() {
        AdminProjectUpdateRequestDto dto = updateRequest(imageKeys(21));

        assertThat(hasViolationOn(validator.validate(dto), "imageKeys")).isTrue();
    }

    @Test
    void presignBatchRequest_files_10개는_상한위반이_아니다() {
        AdminProjectPresignPutBatchRequestDto dto = new AdminProjectPresignPutBatchRequestDto(files(10));

        assertThat(hasViolationOn(validator.validate(dto), "files")).isFalse();
    }

    @Test
    void presignBatchRequest_files_11개는_상한위반이다() {
        AdminProjectPresignPutBatchRequestDto dto = new AdminProjectPresignPutBatchRequestDto(files(11));

        assertThat(hasViolationOn(validator.validate(dto), "files")).isTrue();
    }

    @Test
    void orderPatchRequest_items_200개는_상한위반이_아니다() {
        AdminProjectOrderPatchRequestDto dto = new AdminProjectOrderPatchRequestDto(orderItems(200));

        assertThat(hasViolationOn(validator.validate(dto), "items")).isFalse();
    }

    @Test
    void orderPatchRequest_items_201개는_상한위반이다() {
        AdminProjectOrderPatchRequestDto dto = new AdminProjectOrderPatchRequestDto(orderItems(201));

        assertThat(hasViolationOn(validator.validate(dto), "items")).isTrue();
    }

    private AdminProjectCreateRequestDto createRequest(List<String> imageKeys) {
        return new AdminProjectCreateRequestDto(
                "제목", "요약", "설명", "thumb.png", imageKeys,
                LocalDate.now().plusDays(7), ProjectStatus.OPEN, ProjectCategory.GOODS
        );
    }

    private AdminProjectUpdateRequestDto updateRequest(List<String> imageKeys) {
        return new AdminProjectUpdateRequestDto(
                "제목", "요약", "설명", "thumb.png", imageKeys,
                LocalDate.now().plusDays(7), ProjectStatus.OPEN, ProjectCategory.GOODS
        );
    }

    private List<String> imageKeys(int count) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add("uploads/projects/images/key-" + i + ".png");
        }
        return keys;
    }

    private List<AdminProjectPresignPutBatchRequestDto.FileDto> files(int count) {
        List<AdminProjectPresignPutBatchRequestDto.FileDto> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            files.add(new AdminProjectPresignPutBatchRequestDto.FileDto("file-" + i + ".png", "image/png"));
        }
        return files;
    }

    private List<AdminProjectOrderPatchRequestDto.ItemDto> orderItems(int count) {
        return Collections.nCopies(
                count,
                new AdminProjectOrderPatchRequestDto.ItemDto(1L, true, 1, null)
        );
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }
}
