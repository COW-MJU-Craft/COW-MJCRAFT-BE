package com.example.cowmjucraft.domain.order.dto.request;

import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "수령/배송 정보 요청 DTO")
public record OrderCreateFulfillmentRequestDto(
        @Schema(description = "수령 방법", example = "DELIVERY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "method는 필수입니다.")
        OrderFulfillmentMethod method,

        @Schema(description = "수령인 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "receiverName은 필수입니다.")
        String receiverName,

        @Schema(description = "수령인 연락처", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "receiverPhone은 필수입니다.")
        String receiverPhone,

        @Schema(description = "입력 정보 확인 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "infoConfirmed는 필수입니다.")
        Boolean infoConfirmed,

        @Schema(description = "우편번호", example = "04524")
        String postalCode,

        @Schema(description = "주소", example = "서울시 중구 세종대로 110")
        String addressLine1,

        @Schema(description = "상세 주소", example = "101동 1001호")
        String addressLine2,

        @Schema(description = "배송 메모", example = "부재 시 문 앞에 놓아주세요")
        String deliveryMemo
) {
}
