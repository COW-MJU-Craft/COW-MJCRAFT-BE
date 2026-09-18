package com.example.cowmjucraft.domain.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "고객 주문 목록 항목 응답 DTO")
public record CustomerOrderListItemResponseDto(

        @Schema(description = "주문 ID", example = "12")
        Long orderId,

        @Schema(description = "주문번호", example = "P3-14-0914-1120-483920")
        String orderNo,

        @Schema(description = "주문 상태", example = "PENDING_DEPOSIT")
        String status,

        @Schema(description = "최종 결제 금액", example = "18000")
        int finalAmount,

        @Schema(description = "입금 기한", example = "2026-09-15T23:59:59")
        LocalDateTime depositDeadline,

        @Schema(description = "주문 일시", example = "2026-09-14T11:20:47")
        LocalDateTime createdAt,

        @Schema(description = "상품 요약", example = "명지공방 머그컵 외 1건")
        String itemSummary
) {
}
