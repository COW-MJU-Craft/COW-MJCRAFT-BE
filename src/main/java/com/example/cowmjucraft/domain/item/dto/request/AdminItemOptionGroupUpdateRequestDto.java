package com.example.cowmjucraft.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 옵션 그룹 수정 요청")
public record AdminItemOptionGroupUpdateRequestDto(

        @NotBlank
        @Schema(description = "옵션 그룹명", example = "색상")
        String name,

        @NotNull
        @Schema(description = "필수 선택 여부", example = "true")
        Boolean required,

        @Min(0)
        @Schema(description = "정렬 순서", example = "0")
        int sortOrder
) {
}
