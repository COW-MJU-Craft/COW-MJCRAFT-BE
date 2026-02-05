package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "장바구니 결제 요청 DTO")
public record CartCheckoutRequestDto(

        @NotNull
        @NotEmpty
        @Schema(description = "결제할 장바구니 아이템 ID 목록", example = "[10, 11]")
        List<Long> cartItemIds
) {
}
