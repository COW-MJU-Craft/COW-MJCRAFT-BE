package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "상품 옵션 그룹 응답 (관리자)")
public record AdminItemOptionGroupResponseDto(
        @Schema(description = "옵션 그룹 ID") Long id,
        @Schema(description = "상품 ID") Long itemId,
        @Schema(description = "옵션 그룹명") String name,
        @Schema(description = "필수 선택 여부") boolean required,
        @Schema(description = "정렬 순서") int sortOrder,
        @Schema(description = "옵션 값 목록") List<AdminItemOptionValueResponseDto> values
) {
}
