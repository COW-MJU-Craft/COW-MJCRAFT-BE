package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 주문 정책 수정 요청 DTO")
public record AdminOrderPolicyUpdateRequestDto(

        @NotNull
        @Min(0)
        @Schema(description = "택배 배송비 기본값", example = "3500")
        Integer defaultShippingFee
) {
}
