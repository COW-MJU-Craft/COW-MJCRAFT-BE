package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "바로 구매 요청 DTO")
public record BuyNowRequestDto(

        @NotNull
        @Schema(description = "물품 ID", example = "1")
        Long itemId,

        @NotNull
        @Min(1)
        @Schema(description = "수량", example = "1")
        Integer quantity
) {
}
