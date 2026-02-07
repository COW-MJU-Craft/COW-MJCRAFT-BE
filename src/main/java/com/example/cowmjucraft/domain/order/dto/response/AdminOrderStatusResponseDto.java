package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 주문 상태 변경 응답 DTO")
public record AdminOrderStatusResponseDto(
        @Schema(description = "주문 ID", example = "1")
        Long orderId,

        @Schema(description = "주문 상태", example = "PAID")
        String status
) {
}
