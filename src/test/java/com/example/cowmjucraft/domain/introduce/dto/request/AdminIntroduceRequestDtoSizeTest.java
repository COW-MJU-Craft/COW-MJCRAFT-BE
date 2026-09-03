package com.example.cowmjucraft.domain.introduce.dto.request;

import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceLogoHistoryDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminIntroduceRequestDtoSizeTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void mainUpsertRequest_heroLogoKeys_10개는_상한위반이_아니다() {
        AdminIntroduceMainUpsertRequestDto dto = new AdminIntroduceMainUpsertRequestDto(
                "명지공방", "슬로건", "요약", heroLogoKeys(10)
        );

        assertThat(hasViolationOn(validator.validate(dto), "heroLogoKeys")).isFalse();
    }

    @Test
    void mainUpsertRequest_heroLogoKeys_11개는_상한위반이다() {
        AdminIntroduceMainUpsertRequestDto dto = new AdminIntroduceMainUpsertRequestDto(
                "명지공방", "슬로건", "요약", heroLogoKeys(11)
        );

        assertThat(hasViolationOn(validator.validate(dto), "heroLogoKeys")).isTrue();
    }

    @Test
    void detailUpsertRequest_logoHistories_50개는_상한위반이_아니다() {
        AdminIntroduceDetailUpsertRequestDto dto = new AdminIntroduceDetailUpsertRequestDto(
                null, null, null, logoHistories(50)
        );

        assertThat(hasViolationOn(validator.validate(dto), "logoHistories")).isFalse();
    }

    @Test
    void detailUpsertRequest_logoHistories_51개는_상한위반이다() {
        AdminIntroduceDetailUpsertRequestDto dto = new AdminIntroduceDetailUpsertRequestDto(
                null, null, null, logoHistories(51)
        );

        assertThat(hasViolationOn(validator.validate(dto), "logoHistories")).isTrue();
    }

    private List<String> heroLogoKeys(int count) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add("uploads/introduce/hero-logos/key-" + i + ".png");
        }
        return keys;
    }

    private List<IntroduceLogoHistoryDto> logoHistories(int count) {
        List<IntroduceLogoHistoryDto> histories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            histories.add(new IntroduceLogoHistoryDto(String.valueOf(2000 + i), "key-" + i + ".png", "설명"));
        }
        return histories;
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }
}
