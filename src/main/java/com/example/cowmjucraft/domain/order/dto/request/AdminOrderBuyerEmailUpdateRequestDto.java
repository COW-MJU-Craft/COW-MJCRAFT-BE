package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "주문자 이메일 수정 요청 DTO")
public record AdminOrderBuyerEmailUpdateRequestDto(

        @NotBlank(message = "email은 필수입니다.")
        @Schema(description = "정정할 이메일", example = "yunjin@mju.ac.kr", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {
}
