package com.example.cowmjucraft.domain.mypage.dto.response;

import com.example.cowmjucraft.domain.accounts.user.entity.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageResponseDto(
        @Schema(description = "멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "유저 이름", example = "홍길동")
        String userName,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "소셜 로그인 제공자", example = "NAVER", nullable = true)
        SocialProvider socialProvider,

        @Schema(description = "배송지 정보", nullable = true)
        MyPageAddressResponseDto address
) {
}
