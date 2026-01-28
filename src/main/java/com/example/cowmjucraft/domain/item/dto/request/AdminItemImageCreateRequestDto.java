package com.example.cowmjucraft.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import java.util.List;

@Schema(description = "물품 이미지 등록 요청")
public record AdminItemImageCreateRequestDto(

        @NotEmpty
        @Schema(description = "이미지 목록")
        List<ImageRequestDto> images
) {

    @Schema(description = "이미지 요청 정보")
    public record ImageRequestDto(

            @NotBlank
            @Schema(description = "이미지 S3 key", example = "uploads/items/detail-001.png")
            String imageKey,

            @Min(0)
            @Schema(description = "정렬 순서 (0부터 시작)", example = "0")
            int sortOrder
    ) {
    }
}
