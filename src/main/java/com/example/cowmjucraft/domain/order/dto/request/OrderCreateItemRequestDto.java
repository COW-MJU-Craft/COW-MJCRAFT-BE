package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상품 요청 DTO")
public record OrderCreateItemRequestDto(
        @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "projectItemId는 필수입니다.")
        Long projectItemId,

        @Schema(description = "주문 수량", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 1, message = "quantity는 1 이상이어야 합니다.")
        int quantity
) {
}
