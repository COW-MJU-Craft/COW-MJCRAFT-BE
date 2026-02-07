package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "비회원 주문 생성 응답 DTO")
public record OrderCreateResponseDto(
        @Schema(description = "주문 ID", example = "1")
        Long orderId,

        @Schema(description = "주문번호", example = "ORD-20260207190000-123456")
        String orderNo,

        @Schema(description = "주문 상태", example = "PENDING_DEPOSIT")
        String status,

        @Schema(description = "상품 합계 금액", example = "24000")
        int totalAmount,

        @Schema(description = "배송비", example = "0")
        int shippingFee,

        @Schema(description = "최종 결제 금액", example = "24000")
        int finalAmount,

        @Schema(description = "입금 기한", example = "2026-02-08T23:59:59")
        LocalDateTime depositDeadline,

        @Schema(description = "조회 아이디", example = "guest-mju-001")
        String lookupId,

        @Schema(description = "이메일 링크 조회용 원본 토큰(DB에는 해시만 저장)", example = "raw-view-token-string")
        String viewToken
) {
}
