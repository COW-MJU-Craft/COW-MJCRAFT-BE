package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "관리자 주문 상태 일괄 진행 응답")
public record AdminOrderBulkAdvanceResponseDto(

        @Schema(description = "변경 전 주문 상태", example = "IN_PRODUCTION")
        String previousStatus,

        @Schema(description = "변경 후 주문 상태", example = "READY_TO_SHIP")
        String newStatus,

        @Schema(description = "변경된 주문 ID 목록", example = "[1, 2, 3]")
        List<Long> updatedOrderIds
) {
}
