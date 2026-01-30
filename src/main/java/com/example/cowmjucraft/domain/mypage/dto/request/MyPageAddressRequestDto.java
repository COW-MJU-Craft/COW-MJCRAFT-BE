package com.example.cowmjucraft.domain.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MyPageAddressRequestDto(
        @Schema(description = "수령인 이름", example = "홍길동")
        @NotBlank
        String recipientName,

        @Schema(description = "연락처", example = "01012345678")
        @NotBlank
        String phoneNumber,

        @Schema(description = "우편번호", example = "12345")
        @NotBlank
        String postCode,

        @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
        @NotBlank
        String address
) {
}
