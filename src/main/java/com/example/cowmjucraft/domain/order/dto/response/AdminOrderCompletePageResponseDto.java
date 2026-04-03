package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 주문 완료 페이지 응답 DTO")
public record AdminOrderCompletePageResponseDto(

        @Schema(description = "주문 완료 페이지 ID", example = "1")
        Long id,

        @Schema(description = "주문 완료 메시지 제목", example = "주문이 완료되었습니다.")
        String messageTitle,

        @Schema(
                description = "주문 완료 메시지 설명",
                example = "입금 기한 내에 계좌이체를 완료해 주세요.",
                nullable = true
        )
        String messageDescription,

        @Schema(
                description = "결제 정보",
                example = "국민은행 123456-78-901234 / 예금주: 명지공방"
        )
        String paymentInformation
) {
}