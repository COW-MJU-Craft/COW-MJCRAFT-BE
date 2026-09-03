package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 주문 운송장 정보 수정 요청")
public record AdminOrderTrackingInformationUpdateRequestDto(

        @Size(max = 500, message = "trackingInformation은 500자를 초과할 수 없습니다.")
        @Schema(description = "운송장 정보. null 또는 공백이면 삭제", example = "CJ대한통운 1234-5678-9012", nullable = true)
        String trackingInformation
) {
}
