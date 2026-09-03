package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "관리자 주문 상태 일괄 진행 요청")
public record AdminOrderBulkAdvanceRequestDto(

        @NotEmpty
        @Size(max = 200, message = "orderIds는 200개를 초과할 수 없습니다.")
        @Schema(description = "상태를 다음 단계로 변경할 주문 ID 목록", example = "[1, 2, 3]")
        List<@NotNull Long> orderIds
) {
}
