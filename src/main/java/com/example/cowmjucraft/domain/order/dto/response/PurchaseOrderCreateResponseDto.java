package com.example.cowmjucraft.domain.order.dto.response;

import com.example.cowmjucraft.domain.order.entity.PurchaseOrderStatus;
import com.example.cowmjucraft.domain.order.entity.PurchaseOrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 생성 응답 DTO")
public record PurchaseOrderCreateResponseDto(

        @Schema(description = "주문 ID", example = "100")
        Long orderId,

        @Schema(description = "주문 유형", example = "BUY_NOW")
        PurchaseOrderType orderType,

        @Schema(description = "주문 상태", example = "CREATED")
        PurchaseOrderStatus status,

        @Schema(description = "총 결제 금액", example = "36000")
        int totalPrice,

        @Schema(description = "주문 생성 시각")
        LocalDateTime orderedAt,

        @Schema(description = "주문 항목 목록")
        List<PurchaseOrderLineResponseDto> lines
) {
}
