package com.example.cowmjucraft.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "물품 이미지 정렬 변경 요청")
public record AdminItemImageOrderPatchRequestDto(

        @NotEmpty
        @Schema(description = "정렬 순서를 적용할 이미지 ID 목록", example = "[30, 12, 15]")
        List<Long> imageIds
) {
}
