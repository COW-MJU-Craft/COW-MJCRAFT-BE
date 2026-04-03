package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 완료 페이지 응답 DTO")
public record OrderCompletePageResponseDto(

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
        String paymentInformation,

        @Schema(description = "주문 정보")
        OrderInfo order,

        @ArraySchema(arraySchema = @Schema(description = "주문 상품 목록"))
        List<ItemInfo> items
) {

    public record OrderInfo(
            @Schema(description = "주문 번호", example = "ORD-20260207190000-123456")
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

            @Schema(description = "주문 생성 시각", example = "2026-02-07T19:00:00")
            LocalDateTime createdAt
    ) {
    }

    public record ItemInfo(
            @Schema(description = "프로젝트 상품 ID", example = "1")
            Long projectItemId,

            @Schema(description = "상품명 스냅샷", example = "후드티")
            String itemNameSnapshot,

            @Schema(description = "수량", example = "2")
            int quantity,

            @Schema(description = "단가", example = "12000")
            int unitPrice,

            @Schema(description = "라인 금액", example = "24000")
            int lineAmount
    ) {
    }
}