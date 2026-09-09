package com.example.cowmjucraft.domain.customer.dto.request;

import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 고객 프로필. 환불 은행/계좌·입금자명·수령지는 여기에 포함되지 않는다 —
 * 그 값들은 주문 스냅샷에만 남는다.
 */
@Schema(description = "고객 프로필")
public record CustomerProfileRequestDto(

        @NotBlank(message = "name은 필수입니다.")
        @Schema(description = "이름", example = "김윤진", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @NotBlank(message = "phone은 필수입니다.")
        @Schema(description = "연락처", example = "010-2345-6789", requiredMode = Schema.RequiredMode.REQUIRED)
        String phone,

        @NotNull(message = "buyerType은 필수입니다.")
        @Schema(description = "구분", example = "STUDENT", requiredMode = Schema.RequiredMode.REQUIRED)
        OrderBuyerType buyerType,

        @Schema(description = "캠퍼스", example = "SEOUL")
        String campus,

        @Schema(description = "학과 / 전공", example = "융합소프트웨어학부")
        String departmentOrMajor,

        @Schema(description = "학번", example = "60201234")
        String studentNo
) {
}
