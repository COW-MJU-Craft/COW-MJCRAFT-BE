package com.example.cowmjucraft.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "주문 금액 사전 견적 응답 DTO")
public record OrderQuoteResponseDto(
        List<ItemDto> items,
        int totalAmount,
        int shippingFee,
        int finalAmount
) {
    public record ItemDto(
            Long projectItemId,
            Long projectId,
            String itemName,
            int quantity,
            int unitPrice,
            int lineAmount
    ) {
    }
}
