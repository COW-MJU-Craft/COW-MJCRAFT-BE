package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물품 이미지 정렬 변경 응답")
public record AdminItemImageOrderPatchResponseDto(

        @Schema(description = "물품 ID", example = "10")
        Long itemId,

        @Schema(description = "변경된 이미지 개수", example = "3")
        int updatedCount
) {
}
