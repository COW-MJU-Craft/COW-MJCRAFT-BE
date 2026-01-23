package com.example.cowmjucraft.domain.media.dto.request;

import com.example.cowmjucraft.domain.media.policy.MediaUsageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaPresignPutRequestDto(
        @Schema(description = "원본 파일명(없으면 file로 처리됨)", example = "intro-banner.png")
        String originalFileName,
        @Schema(description = "업로드할 파일의 Content-Type", example = "image/png")
        @NotBlank
        String contentType,
        @Schema(description = "미디어 사용처 유형", example = "INTRO")
        @NotNull
        MediaUsageType usageType
) {
}
