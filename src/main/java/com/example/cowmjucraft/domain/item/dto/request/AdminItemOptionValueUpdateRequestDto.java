package com.example.cowmjucraft.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "상품 옵션 값 수정 요청")
public record AdminItemOptionValueUpdateRequestDto(

        @NotBlank
        @Schema(description = "옵션 값 이름", example = "블랙")
        String name,

        @Min(0)
        @Schema(description = "추가 금액", example = "500")
        int additionalPrice,

        @Min(0)
        @Schema(description = "재고 수량 (null이면 무제한)", example = "10")
        Integer stockQty,

        @Min(0)
        @Schema(description = "정렬 순서", example = "0")
        int sortOrder
) {
}
