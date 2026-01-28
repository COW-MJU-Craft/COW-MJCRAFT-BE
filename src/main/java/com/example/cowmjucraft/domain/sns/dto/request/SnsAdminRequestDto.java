package com.example.cowmjucraft.domain.sns.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "SNS 관리자 단건 등록/수정 요청 DTO")
public record SnsAdminRequestDto(

        @NotBlank
        @Schema(
                description = "SNS 링크 URL",
                example = "https://open.kakao.com/o/xxxx"
        )
        String url
) {
}
