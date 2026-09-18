package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "주문 상품 요청 DTO")
public record OrderCreateItemRequestDto(
        @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "projectItemId는 필수입니다.")
        Long projectItemId,

        @Schema(description = "주문 수량", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 1, message = "quantity는 1 이상이어야 합니다.")
        int quantity,

        @ArraySchema(schema = @Schema(description = "선택한 옵션 값 ID", example = "102"))
        @Size(max = 10, message = "optionValueIds는 10개를 초과할 수 없습니다.")
        @Schema(description = "선택한 옵션 값 ID 목록 (옵션 없는 상품은 생략 가능)")
        List<Long> optionValueIds
) {
}
