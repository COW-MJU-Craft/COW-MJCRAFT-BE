package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물품 이미지 응답")
public record ProjectItemImageResponseDto(

        @Schema(description = "이미지 ID", example = "10")
        Long id,

        @Schema(description = "이미지 URL", example = "https://bucket.s3.amazonaws.com/uploads/items/1/images/uuid-detail.png?X-Amz-Signature=...")
        String imageUrl,

        @Schema(description = "정렬 순서", example = "0")
        int sortOrder
) {
}
