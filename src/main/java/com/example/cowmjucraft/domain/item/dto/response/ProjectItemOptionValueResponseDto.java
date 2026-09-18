package com.example.cowmjucraft.domain.item.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 물품 옵션 값 응답")
public record ProjectItemOptionValueResponseDto(
        @Schema(description = "옵션 값 ID") Long id,
        @Schema(description = "옵션 값 이름") String name,
        @Schema(description = "추가 금액") int additionalPrice,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "재고 수량 (null이면 무제한)") Integer stockQty
) {
}
