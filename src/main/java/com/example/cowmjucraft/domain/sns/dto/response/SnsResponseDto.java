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
                description = "화면에 표시될 제목",
                example = "명지공방 인스타그램"
        )
        String title,

        @Schema(
                description = "SNS 링크 URL",
                example = "https://instagram.com/mju_craft"
        )
        String url,

        @Schema(
                description = "프론트엔드 아이콘 매핑용 키",
                example = "instagram"
        )
        String iconKey
) {
    public static SnsResponseDto from(SnsLink link) {
        return new SnsResponseDto(
                link.getType(),
                link.getTitle(),
                link.getUrl(),
                link.getType().getIconKey()
        );
    }
}