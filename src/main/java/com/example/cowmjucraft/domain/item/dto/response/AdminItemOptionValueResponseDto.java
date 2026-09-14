package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 옵션 값 응답 (관리자)")
public record AdminItemOptionValueResponseDto(
        @Schema(description = "옵션 값 ID") Long id,
        @Schema(description = "옵션 그룹 ID") Long optionGroupId,
        @Schema(description = "옵션 값 이름") String name,
        @Schema(description = "추가 금액") int additionalPrice,
        @Schema(description = "재고 수량 (null=무제한)") Integer stockQty,
        @Schema(description = "정렬 순서") int sortOrder
) {
}
