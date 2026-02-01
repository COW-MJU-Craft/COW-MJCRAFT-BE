package com.example.cowmjucraft.domain.item.dto.response;

import com.example.cowmjucraft.domain.item.entity.ItemImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물품 이미지 응답")
public record ProjectItemImageResponseDto(

        @Schema(description = "이미지 ID", example = "10")
        Long id,

        @Schema(description = "이미지 S3 object key", example = "uploads/items/1/images/uuid-detail.png")
        String imageKey,

        @Schema(description = "정렬 순서", example = "0")
        int sortOrder
) {
    public static ProjectItemImageResponseDto from(ItemImage image) {
        return new ProjectItemImageResponseDto(
                image.getId(),
                image.getImageKey(),
                image.getSortOrder()
        );
    }
}
