package com.example.cowmjucraft.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비회원 주문 조회 요청 DTO")
public record OrderLookupRequestDto(
        @Schema(description = "조회 아이디", example = "guest-mju-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "lookupId는 필수입니다.")
        String lookupId,

        @Schema(description = "조회 비밀번호", example = "Pa$$w0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "password는 필수입니다.")
        String password
) {
}
