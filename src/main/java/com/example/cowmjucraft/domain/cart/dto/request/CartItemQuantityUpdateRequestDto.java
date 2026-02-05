package com.example.cowmjucraft.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 수량 수정 요청 DTO")
public record CartItemQuantityUpdateRequestDto(

        @NotNull
        @Min(1)
        @Schema(description = "수량", example = "3")
        Integer quantity
) {
}
