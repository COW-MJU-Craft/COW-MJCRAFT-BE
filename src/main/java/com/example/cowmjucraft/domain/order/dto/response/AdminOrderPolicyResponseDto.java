package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 주문 정책 응답 DTO")
public record AdminOrderPolicyResponseDto(

        @Schema(description = "주문 정책 ID", example = "1")
        Long id,

        @Schema(description = "택배 배송비 기본값", example = "3500")
        int defaultShippingFee
) {
}
