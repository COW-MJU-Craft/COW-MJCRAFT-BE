package com.example.cowmjucraft.domain.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "장바구니 조회 응답 DTO")
public record CartSummaryResponseDto(

        @Schema(description = "총 아이템 수", example = "2")
        int itemCount,

        @Schema(description = "총 수량", example = "3")
        int totalQuantity,

        @Schema(description = "총 금액", example = "36000")
        int totalPrice,

        @Schema(description = "장바구니 아이템 목록")
        List<CartItemResponseDto> items
) {
}
