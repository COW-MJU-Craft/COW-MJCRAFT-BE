package com.example.cowmjucraft.domain.cart.dto.response;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 아이템 응답 DTO")
public record CartItemResponseDto(

        @Schema(description = "장바구니 아이템 ID", example = "10")
        Long cartItemId,

        @Schema(description = "물품 ID", example = "1")
        Long itemId,

        @Schema(description = "물품명", example = "명지공방 머그컵")
        String itemName,

        @Schema(description = "판매 유형", example = "NORMAL")
        ItemSaleType saleType,

        @Schema(description = "판매 상태", example = "OPEN")
        ItemStatus status,

        @Schema(description = "대표 이미지 URL", example = "https://bucket.s3.amazonaws.com/uploads/items/1/thumbnail/uuid-thumbnail.png")
        String thumbnailUrl,

        @Schema(description = "단가", example = "12000")
        int unitPrice,

        @Schema(description = "수량", example = "2")
        int quantity,

        @Schema(description = "소계", example = "24000")
        int linePrice
) {
}
