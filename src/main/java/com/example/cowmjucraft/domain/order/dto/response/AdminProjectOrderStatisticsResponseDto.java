package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 프로젝트 주문 통계 응답")
public record AdminProjectOrderStatisticsResponseDto(

        @Schema(description = "통계 포함 상태의 프로젝트 주문 수", example = "42")
        long orderCount,

        @Schema(description = "통계 포함 상태인 해당 프로젝트 상품의 주문 금액 합계", example = "1260000")
        long totalOrderAmount
) {
}
