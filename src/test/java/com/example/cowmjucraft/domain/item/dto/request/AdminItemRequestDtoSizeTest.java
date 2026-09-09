package com.example.cowmjucraft.domain.item.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminItemRequestDtoSizeTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void presignBatchRequest_files_10개는_상한위반이_아니다() {
        AdminItemPresignPutBatchRequestDto dto = new AdminItemPresignPutBatchRequestDto(files(10));

        assertThat(hasViolationOn(validator.validate(dto), "files")).isFalse();
    }

    @Test
    void presignBatchRequest_files_11개는_상한위반이다() {
        AdminItemPresignPutBatchRequestDto dto = new AdminItemPresignPutBatchRequestDto(files(11));

        assertThat(hasViolationOn(validator.validate(dto), "files")).isTrue();
    }

    @Test
    void imageCreateRequest_images_20개는_상한위반이_아니다() {
        AdminItemImageCreateRequestDto dto = new AdminItemImageCreateRequestDto(images(20));

        assertThat(hasViolationOn(validator.validate(dto), "images")).isFalse();
    }

    @Test
    void imageCreateRequest_images_21개는_상한위반이다() {
        AdminItemImageCreateRequestDto dto = new AdminItemImageCreateRequestDto(images(21));

        assertThat(hasViolationOn(validator.validate(dto), "images")).isTrue();
    }

    @Test
    void imageOrderPatchRequest_imageIds_20개는_상한위반이_아니다() {
        AdminItemImageOrderPatchRequestDto dto = new AdminItemImageOrderPatchRequestDto(imageIds(20));

        assertThat(hasViolationOn(validator.validate(dto), "imageIds")).isFalse();
    }

    @Test
    void imageOrderPatchRequest_imageIds_21개는_상한위반이다() {
        AdminItemImageOrderPatchRequestDto dto = new AdminItemImageOrderPatchRequestDto(imageIds(21));

        assertThat(hasViolationOn(validator.validate(dto), "imageIds")).isTrue();
    }

    private List<AdminItemPresignPutBatchRequestDto.FileDto> files(int count) {
        List<AdminItemPresignPutBatchRequestDto.FileDto> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            files.add(new AdminItemPresignPutBatchRequestDto.FileDto("file-" + i + ".png", "image/png"));
        }
        return files;
    }

    private List<AdminItemImageCreateRequestDto.ImageRequestDto> images(int count) {
        List<AdminItemImageCreateRequestDto.ImageRequestDto> images = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            images.add(new AdminItemImageCreateRequestDto.ImageRequestDto("uploads/items/1/images/key-" + i + ".png", i));
        }
        return images;
    }

    private List<Long> imageIds(int count) {
        List<Long> ids = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            ids.add(i);
        }
        return ids;
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }
}
