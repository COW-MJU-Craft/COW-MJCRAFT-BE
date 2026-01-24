package com.example.cowmjucraft.domain.media.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Presign PUT batch 응답")
public record MediaPresignPutResponseDto(
        @Schema(description = "발급 결과 목록")
        List<ItemDto> items
) {
    @Schema(description = "발급 결과 항목")
    public record ItemDto(
            @Schema(description = "원본 파일명", example = "test.png")
            String fileName,
            @Schema(description = "S3 object key", example = "uploads/intro/uuid-test.png")
            String key,
            @Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
            String uploadUrl,
            @Schema(description = "presign URL 만료 시간(초)", example = "300")
            int expiresInSeconds
    ) {
    }
}
