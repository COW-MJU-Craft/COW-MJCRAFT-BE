package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 라인 응답 DTO")
public record PurchaseOrderLineResponseDto(

        @Schema(description = "물품 ID", example = "1")
        Long itemId,

        @Schema(description = "물품명", example = "명지공방 머그컵")
        String itemName,

        @Schema(description = "단가", example = "12000")
        int unitPrice,

        @Schema(description = "수량", example = "2")
        int quantity,

        @Schema(description = "소계", example = "24000")
        int linePrice
) {
}
