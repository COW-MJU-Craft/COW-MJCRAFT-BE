package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 주문 취소 요청 DTO")
public record AdminOrderCancelRequestDto(
        @Schema(description = "취소 사유(선택)", example = "고객 요청", nullable = true)
        String reason
) {
}
