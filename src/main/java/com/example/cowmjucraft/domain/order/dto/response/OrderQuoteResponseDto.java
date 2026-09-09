package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "주문 금액 사전 견적 응답 DTO")
public record OrderQuoteResponseDto(

        @Schema(description = "견적 상품 목록(동일 상품은 수량이 합산되어 한 줄로 반환)")
        List<ItemDto> items,

        @Schema(description = "상품 금액 합계", example = "6000")
        int totalAmount,

        @Schema(description = "배송비(현장 수령이면 0)", example = "3500")
        int shippingFee,

        @Schema(description = "최종 결제 금액(상품 금액 + 배송비)", example = "9500")
        int finalAmount
) {

    @Schema(description = "견적 상품 응답 DTO")
    public record ItemDto(

            @Schema(description = "상품 ID", example = "1")
            Long projectItemId,

            @Schema(description = "상품이 속한 프로젝트 ID", example = "10")
            Long projectId,

            @Schema(description = "상품명", example = "명지공방 키링")
            String itemName,

            @Schema(description = "주문 수량", example = "2")
            int quantity,

            @Schema(description = "현재 서버 기준 단가", example = "3000")
            int unitPrice,

            @Schema(description = "단가 × 수량", example = "6000")
            int lineAmount
    ) {
    }
}
