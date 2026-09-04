package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자 주문 목록 아이템 응답 DTO")
public record AdminOrderListItemResponseDto(
        @Schema(description = "주문 ID", example = "1")
        Long orderId,

        @Schema(description = "주문 번호", example = "P10-12-09041530-482731")
        String orderNo,

        @Schema(description = "주문번호를 발급한 대표 프로젝트 ID", example = "10")
        Long representativeProjectId,

        @Schema(description = "대표 프로젝트 내 주문 순번", example = "12")
        long projectOrderNo,

        @Schema(description = "주문 상태", example = "PENDING_DEPOSIT")
        String status,

        @Schema(description = "최종 결제 금액", example = "24000")
        int finalAmount,

        @Schema(description = "배송비", example = "3500")
        int shippingFee,

        @Schema(description = "입금자명", example = "홍길동")
        String depositorName,

        @Schema(description = "구매자 이름", example = "홍길동")
        String buyerName,

        @Schema(description = "구매자 연락처", example = "010-1234-5678")
        String buyerPhone,

        @Schema(description = "주문 생성 시각", example = "2026-02-07T19:00:00")
        LocalDateTime createdAt,

        @Schema(description = "입금 기한", example = "2026-02-08T23:59:59")
        LocalDateTime depositDeadline
) {
}
