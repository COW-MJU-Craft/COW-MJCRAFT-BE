package com.example.cowmjucraft.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 아이템 추가 요청 DTO")
public record CartItemCreateRequestDto(

        @NotNull
        @Schema(description = "물품 ID", example = "1")
        Long itemId,

        @NotNull
        @Min(1)
        @Schema(description = "수량", example = "2")
        Integer quantity
) {
}
