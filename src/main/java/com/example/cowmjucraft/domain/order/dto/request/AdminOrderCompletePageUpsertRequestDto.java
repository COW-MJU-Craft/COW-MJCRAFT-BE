package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 주문 완료 페이지 저장/수정 요청 DTO")
public record AdminOrderCompletePageUpsertRequestDto(

        @NotBlank
        @Schema(description = "주문 완료 메시지 제목", example = "주문이 완료되었습니다.")
        String messageTitle,

        @Schema(
                description = "주문 완료 메시지 설명",
                example = "입금 기한 내에 계좌이체를 완료해 주세요.",
                nullable = true
        )
        String messageDescription,

        @NotBlank
        @Schema(
                description = "결제 정보",
                example = "국민은행 123456-78-901234 / 예금주: 명지공방"
        )
        String paymentInformation
) {
}