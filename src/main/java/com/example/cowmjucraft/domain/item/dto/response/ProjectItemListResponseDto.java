package com.example.cowmjucraft.domain.item.dto.response;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 물품 목록 응답")
public record ProjectItemListResponseDto(

        @Schema(description = "물품 ID", example = "1")
        Long id,

        @Schema(description = "물품명", example = "명지공방 머그컵")
        String name,

        @Schema(description = "물품 한줄 설명", example = "캠퍼스 감성을 담은 데일리 머그컵")
        String summary,

        @Schema(description = "가격", example = "12000")
        int price,

        @Schema(description = "판매 유형", example = "NORMAL")
        ItemSaleType saleType,

        @Schema(description = "아이템 타입", example = "PHYSICAL")
        ItemType itemType,

        @Schema(description = "상태", example = "OPEN")
        ItemStatus status,

        @Schema(
                description = "대표 이미지 S3 object key",
                example = "uploads/items/1/thumbnail/uuid-thumbnail.png",
                deprecated = true
        )
        String thumbnailKey,

        @Schema(description = "대표 이미지 URL", example = "https://bucket.s3.amazonaws.com/uploads/items/1/thumbnail/uuid-thumbnail.png?X-Amz-Signature=...")
        String thumbnailUrl,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "재고 수량 (NORMAL 전용)", example = "50")
        Integer stockQty,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "목표 수량 (GROUPBUY 전용)", example = "100")
        Integer targetQty,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "현재 모금 수량 (GROUPBUY 전용)", example = "40")
        Integer fundedQty,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "달성률(%) (GROUPBUY 전용)", example = "40.0")
        Double achievementRate,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "남은 수량 (GROUPBUY 전용)", example = "60")
        Integer remainingQty
) {
}
