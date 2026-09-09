package com.example.cowmjucraft.domain.order.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreateRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void items_50개는_상한위반이_아니다() {
        OrderCreateRequestDto dto = requestWithItems(50);

        Set<ConstraintViolation<OrderCreateRequestDto>> violations = validator.validate(dto);

        assertThat(hasViolationOn(violations, "items")).isFalse();
    }

    @Test
    void items_51개는_상한위반이다() {
        OrderCreateRequestDto dto = requestWithItems(51);

        Set<ConstraintViolation<OrderCreateRequestDto>> violations = validator.validate(dto);

        assertThat(hasViolationOn(violations, "items")).isTrue();
    }

    private OrderCreateRequestDto requestWithItems(int count) {
        List<OrderCreateItemRequestDto> items = Collections.nCopies(count, new OrderCreateItemRequestDto(1L, 1));
        return new OrderCreateRequestDto(
                "guest-id", "password", "홍길동", true, true, true, items, null, null
        );
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }
}
