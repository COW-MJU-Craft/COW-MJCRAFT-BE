package com.example.cowmjucraft.domain.sns.dto.request;

import com.example.cowmjucraft.domain.sns.entity.SnsType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "SNS 관리자 전체 교체 요청 DTO")
public record SnsAdminRequestDto(

        @NotNull
        @Schema(
                description = "SNS 타입",
                example = "INSTAGRAM",
                allowableValues = {"INSTAGRAM", "KAKAO", "ETC"}
        )
        SnsType type,

        @NotBlank
        @Schema(
                description = "화면에 표시될 제목",
                example = "명지공방 인스타그램"
        )
        String title,

        @NotBlank
        @Schema(
                description = "SNS 링크 URL",
                example = "https://instagram.com/mju_craft"
        )
        String url,

        @Min(1)
        @Schema(
                description = "정렬 순서 (작을수록 먼저 노출)",
                example = "1"
        )
        int sortOrder,

        @Schema(
                description = "노출 여부",
                example = "true"
        )
        boolean active
) {
}