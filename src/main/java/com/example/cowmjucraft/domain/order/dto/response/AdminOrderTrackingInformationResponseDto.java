package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 주문 운송장 정보 응답")
public record AdminOrderTrackingInformationResponseDto(

        @Schema(description = "주문 ID", example = "10")
        Long orderId,

        @Schema(description = "운송장 정보", example = "CJ대한통운 1234-5678-9012", nullable = true)
        String trackingInformation
) {
}
