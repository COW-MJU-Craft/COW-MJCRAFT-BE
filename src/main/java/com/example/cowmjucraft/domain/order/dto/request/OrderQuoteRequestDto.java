package com.example.cowmjucraft.domain.order.dto.request;

import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "주문 금액 사전 견적 요청 DTO")
public record OrderQuoteRequestDto(
        @ArraySchema(
                schema = @Schema(implementation = OrderCreateItemRequestDto.class),
                arraySchema = @Schema(description = "주문 상품 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        )
        @Valid
        @NotEmpty(message = "items는 1개 이상이어야 합니다.")
        @Size(max = 50, message = "items는 50개를 초과할 수 없습니다.")
        List<OrderCreateItemRequestDto> items,

        @Schema(description = "수령 방식", example = "DELIVERY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "fulfillmentMethod는 필수입니다.")
        OrderFulfillmentMethod fulfillmentMethod
) {
}
