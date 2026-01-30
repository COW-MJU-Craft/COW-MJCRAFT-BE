package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "브랜드명/슬로건")
public record IntroduceBrandResponseDto(
        @Schema(description = "브랜드명", example = "명지공방")
        String title,

        @Schema(description = "슬로건", example = "학생들이 만드는 작은 브랜드", nullable = true)
        String subtitle
) {
}
