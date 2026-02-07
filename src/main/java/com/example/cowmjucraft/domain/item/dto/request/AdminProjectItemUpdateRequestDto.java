package com.example.cowmjucraft.domain.item.dto.request;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "프로젝트 물품 수정 요청")
public record AdminProjectItemUpdateRequestDto(

        @NotBlank
        @Schema(description = "물품명", example = "명지공방 머그컵")
        String name,

        @Size(max = 200)
        @Schema(description = "물품 한줄 설명", example = "캠퍼스 감성을 담은 데일리 머그컵")
        String summary,

        @Schema(description = "물품 설명", example = "캠퍼스 감성을 담은 머그컵입니다.")
        String description,

        @Min(0)
        @Schema(description = "가격", example = "12000")
        int price,

        @NotNull
        @Schema(description = "판매 유형", example = "NORMAL")
        ItemSaleType saleType,

        @NotNull
        @Schema(description = "상태", example = "OPEN")
        ItemStatus status,

        @Schema(description = "대표 이미지 S3 object key (PHYSICAL 전용)", example = "uploads/items/1/thumbnail/uuid-thumbnail.png")
        String thumbnailKey,

        @Schema(description = "목표 수량 (GROUPBUY 전용)", example = "100")
        Integer targetQty,

        @NotNull
        @Schema(description = "현재 모금 수량", example = "0")
        Integer fundedQty,

        @Schema(description = "아이템 타입", example = "PHYSICAL")
        ItemType itemType,

        @Schema(description = "저널 파일 S3 object key (presign-put 결과 key)", example = "uploads/projects/1/journals/uuid-journal.pdf")
        String journalFileKey
) {
}
