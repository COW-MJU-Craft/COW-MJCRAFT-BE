package com.example.cowmjucraft.domain.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageAddressResponseDto(
        @Schema(description = "수령인 이름", example = "홍길동")
        String recipientName,

        @Schema(description = "연락처", example = "01012345678")
        String phoneNumber,

        @Schema(description = "우편번호", example = "12345")
        String postCode,

        @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
        String address
) {
}
