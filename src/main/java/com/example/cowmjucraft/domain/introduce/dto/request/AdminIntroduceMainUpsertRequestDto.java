package com.example.cowmjucraft.domain.introduce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "소개 메인 정보 저장 요청 DTO")
public record AdminIntroduceMainUpsertRequestDto(

        @NotBlank
        @Schema(description = "브랜드명", example = "명지공방")
        String title,

        @Schema(description = "슬로건", example = "학생들이 만드는 작은 브랜드", nullable = true)
        String subtitle,

        @Schema(description = "한 줄 요약", example = "명지공방은 학생들이 직접 만드는 공방 브랜드입니다.", nullable = true)
        String summary,

        @Schema(
                description = "메인 히어로 로고 S3 object key 목록",
                example = "[\"uploads/introduce/hero-logos/uuid-logo-1.png\", \"uploads/introduce/hero-logos/uuid-logo-2.png\"]",
                nullable = true
        )
        List<String> heroLogoKeys
) {
}
