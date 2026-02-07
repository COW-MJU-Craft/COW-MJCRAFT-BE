package com.example.cowmjucraft.domain.order.dto.request;

import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문자 정보 요청 DTO")
public record OrderCreateBuyerRequestDto(
        @Schema(description = "주문자 유형", example = "STUDENT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "buyerType은 필수입니다.")
        OrderBuyerType buyerType,

        @Schema(description = "캠퍼스", example = "SEOUL")
        String campus,

        @Schema(description = "주문자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "name은 필수입니다.")
        String name,

        @Schema(description = "학과/전공", example = "컴퓨터공학과")
        String departmentOrMajor,

        @Schema(description = "학번", example = "60123456")
        String studentNo,

        @Schema(description = "주문자 연락처", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "phone은 필수입니다.")
        String phone,

        @Schema(description = "환불 은행", example = "국민은행", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "refundBank는 필수입니다.")
        String refundBank,

        @Schema(description = "환불 계좌", example = "123456-78-901234", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "refundAccount는 필수입니다.")
        String refundAccount,

        @Schema(description = "유입 경로", example = "instagram")
        String referralSource,

        @Schema(description = "이메일", example = "hong@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "email 형식이 올바르지 않습니다.")
        @NotBlank(message = "email은 필수입니다.")
        String email
) {
}
