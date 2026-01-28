package com.example.cowmjucraft.domain.sns.dto.response;

import com.example.cowmjucraft.domain.sns.entity.SnsLink;
import com.example.cowmjucraft.domain.sns.entity.SnsType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SNS 소개 응답 DTO")
public record SnsResponseDto(

        @Schema(
                description = "SNS 타입",
                example = "INSTAGRAM"
        )
        SnsType type,

        @Schema(
                description = "SNS 링크 URL",
                example = "https://instagram.com/mju_craft"
        )
        String url
) {
    public static SnsResponseDto from(SnsLink link) {
        return new SnsResponseDto(
                link.getType(),
                link.getUrl()
        );
    }
}
